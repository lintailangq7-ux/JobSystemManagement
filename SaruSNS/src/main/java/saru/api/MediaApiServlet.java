package saru.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.Config;
import saru.dao.PostDao;

/** GET /api/media/{id} → 画像・音声のファイル(権限を確認してから返す) */
@WebServlet("/api/media/*")
public class MediaApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Path uploadRoot;

	@Override
	public void init() throws ServletException {
		try {
			uploadRoot = Config.load(getServletContext()).uploadDir;
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Integer userId = (Integer) req.getAttribute("apiUserId");
		String path = req.getPathInfo() == null ? "" : req.getPathInfo().substring(1);
		int mediaId;
		try {
			mediaId = Integer.parseInt(path);
		} catch (NumberFormatException e) {
			res.sendError(404);
			return;
		}
		try {
			PostDao.MediaRow m = PostDao.findVisibleMedia(userId, mediaId);
			if (m == null) { // 存在しない、または、公開範囲で見てはいけない投稿
				res.sendError(404);
				return;
			}
			String viewerType = PostDao.accountType(userId);
			boolean blocked = ("mizaru".equals(viewerType) && "image".equals(m.type()))
					|| ("kikazaru".equals(viewerType) && "audio".equals(m.type()));
			if (blocked) {
				res.sendError(403);
				return;
			}
			
			
			Path root = uploadRoot.normalize();
			Path file = root.resolve("image".equals(m.type()) ? "images" : "audio").resolve(m.fileName()).normalize();
			if (!file.startsWith(root) || !Files.isRegularFile(file)) {
				res.sendError(404);
				return;
			}
			send(req, res, file);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	/** 投稿の本文を見てよいか判定 */
	public static boolean canSeePostText(int viewerId, int postAuthorId, String viewerType) {
	    // 自分の投稿は常に見える
	    if (viewerId == postAuthorId) return true;
	    // 言わざる以外の人は見える → 言わざるは本文が見えない
	    return !"iwazaru".equals(viewerType);
	}

	/** ファイルを返す。音声のシークや、iPhone(Safari)のために、Range にも対応する */
	private static void send(HttpServletRequest req, HttpServletResponse res, Path file) throws IOException {
		long size = Files.size(file);
		res.setContentType(contentType(file.getFileName().toString()));
		res.setHeader("Accept-Ranges", "bytes");
		res.setHeader("Cache-Control", "private, max-age=3600");

		long start = 0;
		long end = size - 1;
		String range = req.getHeader("Range");
		if (range != null && range.startsWith("bytes=") && range.indexOf('-') > 0) {
			try {
				String spec = range.substring(6);
				int dash = spec.indexOf('-');
				String a = spec.substring(0, dash);
				String b = spec.substring(dash + 1);
				if (a.isEmpty()) {
					start = Math.max(0, size - Long.parseLong(b)); // "bytes=-500" は、末尾の500バイト
				} else {
					start = Long.parseLong(a);
					if (!b.isEmpty()) end = Math.min(Long.parseLong(b), size - 1);
				}
				if (start > end || start >= size) {
					res.setStatus(416);
					res.setHeader("Content-Range", "bytes */" + size);
					return;
				}
				res.setStatus(206);
				res.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + size);
			} catch (NumberFormatException e) {
				start = 0;
				end = size - 1; // 読めない Range は、無視して全部返す
			}
		}
		long length = end - start + 1;
		res.setContentLengthLong(length);
		try (InputStream in = Files.newInputStream(file); OutputStream out = res.getOutputStream()) {
			in.skipNBytes(start);
			byte[] buf = new byte[8192];
			long left = length;
			while (left > 0) {
				int n = in.read(buf, 0, (int) Math.min(buf.length, left));
				if (n < 0) break;
				out.write(buf, 0, n);
				left -= n;
			}
		}
	}

	private static String contentType(String name) {
		String n = name.toLowerCase(Locale.ROOT);
		if (n.endsWith(".jpg")) return "image/jpeg";
		if (n.endsWith(".png")) return "image/png";
		if (n.endsWith(".gif")) return "image/gif";
		if (n.endsWith(".wav")) return "audio/wav";
		if (n.endsWith(".mp3")) return "audio/mpeg";
		if (n.endsWith(".m4a")) return "audio/mp4";
		if (n.endsWith(".aac")) return "audio/aac";
		if (n.endsWith(".ogg")) return "audio/ogg";
		return "application/octet-stream";
	}
}