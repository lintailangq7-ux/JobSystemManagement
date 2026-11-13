package saru.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import saru.Config;
import saru.dao.PostDao;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 16 * 1024 * 1024)
@WebServlet("/api/posts")
public class PostsApiServlet extends HttpServlet {
	// ...(今ある doGet などは、そのまま)...

	private static final Set<String> VISIBILITIES = Set.of("public", "followers", "private");
	private static final Set<String> AUDIO_EXT = Set.of("wav", "mp3", "m4a", "aac", "ogg");
	private static final long MAX_IMAGE = 5L * 1024 * 1024;
	private static final int MAX_BODY = 1000;
	private Config config;

	/** 画像・音声の保存先。 */
	@Override
	public void init() throws ServletException{
		try {
			config = Config.load(getServletContext());
		} catch (Exception e) {
			throw new ServletException("Config の読み込みに失敗", e);
		}
	}
	
	private Path uploadRoot() {
		return config.uploadDir;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8"); // multipart の文字を、文字化けさせない。最初に呼ぶ
		Integer userId = (Integer) req.getAttribute("apiUserId"); // ApiFilter が入れる

		String ct = req.getContentType();
		if (ct == null || !ct.startsWith("multipart/")) {
			ApiJson.error(res, 400, "multipart/form-data で送ってください");
			return;
		}
		String body = trim(req.getParameter("body"));
		String visibility = trim(req.getParameter("visibility"));
		if (visibility.isEmpty()) visibility = "public";
		if (!VISIBILITIES.contains(visibility)) {
			ApiJson.error(res, 400, "公開範囲が正しくありません");
			return;
		}
		if (body.codePointCount(0, body.length()) > MAX_BODY) {
			ApiJson.error(res, 400, "本文は" + MAX_BODY + "文字までです");
			return;
		}

		Part imagePart;
		Part audioPart;
		try {
			imagePart = req.getPart("image");
			audioPart = req.getPart("audio");
		} catch (IllegalStateException e) { // @MultipartConfig の上限を超えた
			ApiJson.error(res, 413, "ファイルが大きすぎます(画像は5MB、音声は10MBまで)");
			return;
		}
		boolean hasImage = imagePart != null && imagePart.getSize() > 0;
		boolean hasAudio = audioPart != null && audioPart.getSize() > 0;
		if (body.isEmpty() && !hasImage && !hasAudio) {
			ApiJson.error(res, 400, "本文・画像・音声のどれかを入れてください");
			return;
		}

		// ここで、アカウントの種類(見ざる・言わざる・聞かざる)による投稿の制限をかける(未確定)

		List<Path> saved = new ArrayList<>();
		List<PostDao.NewMedia> media = new ArrayList<>();
		try {
			if (hasImage) {
				byte[] data = imagePart.getInputStream().readAllBytes();
				if (data.length > MAX_IMAGE) throw new IllegalArgumentException("画像は5MBまでです");
				String ext = imageExtension(data);
				if (ext == null) throw new IllegalArgumentException("画像は jpg / png / gif のみ使えます");
				Path file = newFile("images", ext);
				Files.write(file, data);
				saved.add(file);
				media.add(new PostDao.NewMedia("image", file.getFileName().toString()));
			}
			if (hasAudio) {
				String ext = extensionOf(audioPart.getSubmittedFileName());
				if (!AUDIO_EXT.contains(ext)) throw new IllegalArgumentException("音声は wav / mp3 / m4a / aac / ogg のみ使えます");
				Path file = newFile("audio", ext);
				try (InputStream in = audioPart.getInputStream()) {
					Files.copy(in, file);
				}
				saved.add(file);
				media.add(new PostDao.NewMedia("audio", file.getFileName().toString()));
			}
			int postId = PostDao.create(userId, body, visibility, media);
			ApiJson.write(res, 201, "{\"id\":" + postId + "}");
		} catch (IllegalArgumentException e) {
			deleteAll(saved);
			ApiJson.error(res, 400, e.getMessage());
		} catch (SQLException | IOException | RuntimeException e) {
			deleteAll(saved);
			throw new ServletException(e);
		}
	}

	private  Path newFile(String folder, String ext) throws IOException {
		Path dir = uploadRoot().resolve(folder);
		Files.createDirectories(dir);
		return dir.resolve(UUID.randomUUID() + "." + ext); // 名前はサーバーが決める(ユーザーの名前は使わない)
	}

	/** 画像の中身を見て、拡張子を決める(jpg / png / gif 以外は null) */
	private static String imageExtension(byte[] data) throws IOException {
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (!readers.hasNext()) return null;
			ImageReader reader = readers.next();
			try {
				return switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
					case "jpeg" -> "jpg";
					case "png" -> "png";
					case "gif" -> "gif";
					default -> null;
				};
			} finally {
				reader.dispose();
			}
		}
	}

	private static String extensionOf(String name) {
		if (name == null) return "";
		int i = name.lastIndexOf('.');
		return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
	}

	private static void deleteAll(List<Path> files) {
		for (Path p : files) {
			try {
				Files.deleteIfExists(p);
			} catch (IOException ignored) {
				// 消せなくても、投稿の結果には影響しない
			}
		}
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}
}