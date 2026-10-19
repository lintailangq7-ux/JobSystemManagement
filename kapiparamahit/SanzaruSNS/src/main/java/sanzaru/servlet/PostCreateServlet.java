package sanzaru.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import sanzaru.AccountType;
import sanzaru.dao.Db;
import sanzaru.dao.PostDao;
import sanzaru.model.User;

/** 投稿・コメントの作成 (画像・音声のアップロードもここ) */
@WebServlet("/post/new")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 16 * 1024 * 1024)
public class PostCreateServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_POST_LENGTH = 280;
	private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;
	private static final long MAX_AUDIO_BYTES = 10 * 1024 * 1024;
	private static final SecureRandom RANDOM = new SecureRandom();

	private static final Map<String, String> IMAGE_TYPES = Map.of(
			"image/png", "png", "image/jpeg", "jpg", "image/gif", "gif", "image/webp", "webp");
	private static final Map<String, String> AUDIO_TYPES = Map.ofEntries(
			Map.entry("audio/webm", "webm"), Map.entry("audio/ogg", "ogg"), Map.entry("audio/mpeg", "mp3"),
			Map.entry("audio/mp3", "mp3"), Map.entry("audio/mp4", "m4a"), Map.entry("audio/x-m4a", "m4a"),
			Map.entry("audio/aac", "aac"), Map.entry("audio/wav", "wav"), Map.entry("audio/x-wav", "wav"),
			Map.entry("audio/wave", "wav"));

	/** 入力エラー (メッセージを画面に出す) */
	private static class InputException extends Exception {
		private static final long serialVersionUID = 1L;

		InputException(String message) {
			super(message);
		}
	}

	private record SavedMedia(String file, String mime) {}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		User me = me(req);
		String text = LoginServlet.trim(req.getParameter("text"));
		Part image = filePart(req, "image");
		Part audio = filePart(req, "audio");
		Integer replyTo = parseId(req.getParameter("replyTo"));
		try {
			if (text.isEmpty() && image == null && audio == null)
				throw new InputException("文字・画像・音声のどれかを入れてください");
			if (text.codePointCount(0, text.length()) > MAX_POST_LENGTH)
				throw new InputException(MAX_POST_LENGTH + "文字以内にしてください");
			if (replyTo != null) {
				if (PostDao.ownerOf(replyTo) == null) throw new InputException("返信先の投稿がありません");
				// コメントではアカウントタイプで禁止されたメディアは使えない
				AccountType type = me.getType();
				boolean used = switch (type.getBlocked()) {
					case "text" -> !text.isEmpty();
					case "image" -> image != null;
					default -> audio != null;
				};
				if (used) throw new InputException(type.getLabel() + "アカウントはコメントに" + type.getBlockedLabel() + "を使えません");
			}
			SavedMedia img = image == null ? null : saveMedia(image, IMAGE_TYPES, MAX_IMAGE_BYTES, "画像");
			SavedMedia aud = audio == null ? null : saveMedia(audio, AUDIO_TYPES, MAX_AUDIO_BYTES, "音声");
			PostDao.create(me.getId(), text,
					img == null ? null : img.file(), img == null ? null : img.mime(),
					aud == null ? null : aud.file(), aud == null ? null : aud.mime(), replyTo);
		} catch (InputException e) {
			flashError(req, e.getMessage());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		redirectBack(req, res, null);
	}

	/** ファイルが選ばれていなければ null */
	private static Part filePart(HttpServletRequest req, String name) throws IOException, ServletException {
		Part p = req.getPart(name);
		return p == null || p.getSize() == 0 ? null : p;
	}

	/** 形式と大きさを確認して、アップロード用フォルダに保存する */
	private static SavedMedia saveMedia(Part part, Map<String, String> allowed, long maxBytes, String label)
			throws InputException, IOException {
		String mime = part.getContentType() == null ? "" : part.getContentType().split(";")[0].trim().toLowerCase();
		String ext = allowed.get(mime);
		if (ext == null) throw new InputException("この形式の" + label + "は使えません (" + mime + ")");
		if (part.getSize() > maxBytes) throw new InputException(label + "は" + maxBytes / 1024 / 1024 + "MBまでです");
		byte[] b = new byte[16];
		RANDOM.nextBytes(b);
		String file = HexFormat.of().formatHex(b) + "." + ext;
		try (InputStream in = part.getInputStream()) {
			Files.copy(in, Db.config().uploadDir.resolve(file));
		}
		return new SavedMedia(file, mime);
	}
}
