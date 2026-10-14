package sanzaru.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.Db;
import sanzaru.dao.PostDao;

/** 自分の投稿の削除 (コメントも一緒に削除) */
@WebServlet("/post/delete")
public class PostDeleteServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Integer id = parseId(req.getParameter("id"));
		try {
			Integer owner = id == null ? null : PostDao.ownerOf(id);
			if (owner == null) {
				flashError(req, "投稿が見つかりません");
				redirectBack(req, res, null);
				return;
			}
			if (owner != me(req).getId()) {
				flashError(req, "自分の投稿のみ削除できます");
				redirectBack(req, res, null);
				return;
			}
			PostDao.Deleted deleted = PostDao.delete(id);
			for (String file : deleted.files()) {
				Files.deleteIfExists(Db.config().uploadDir.resolve(file));
			}
			flash(req, "削除しました");
			// 消した投稿の詳細画面には戻れないのでホームへ
			String back = backPath(req);
			for (int d : deleted.ids()) {
				if (back.equals("/post?id=" + d)) back = "/";
			}
			redirect(req, res, back);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}
