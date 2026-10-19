package sanzaru.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.Db;
import sanzaru.dao.PostDao;
import sanzaru.model.User;

/**
 * アップロードされた画像・音声 (GET /media/ファイル名)
 * 見る権限がないタイプのアカウントにはファイル自体を渡さない (自分の投稿は除く)
 */
@WebServlet("/media/*")
public class MediaServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String name = req.getPathInfo() == null ? "" : req.getPathInfo().substring(1);
		if (!name.matches("[a-f0-9]{32}\\.[a-z0-9]+")) {
			res.sendError(404);
			return;
		}
		PostDao.Media media;
		try {
			media = PostDao.findMedia(name);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		Path file = Db.config().uploadDir.resolve(name);
		User me = me(req);
		boolean allowed = media != null && (media.userId() == me.getId() || me.getType().canSee(media.kind()));
		if (!allowed || !Files.exists(file)) {
			res.sendError(404);
			return;
		}
		res.setContentType(media.mime());
		res.setContentLengthLong(Files.size(file));
		res.setHeader("X-Content-Type-Options", "nosniff");
		res.setHeader("Cache-Control", "private, no-store");
		Files.copy(file, res.getOutputStream());
	}
}
