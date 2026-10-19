package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.PostDao;

/** いいね / いいね解除 */
@WebServlet("/post/like")
public class LikeServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Integer id = parseId(req.getParameter("id"));
		try {
			if (id == null || PostDao.ownerOf(id) == null) {
				flashError(req, "投稿が見つかりません");
				redirectBack(req, res, null);
				return;
			}
			PostDao.toggleLike(me(req).getId(), id);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		// 押した投稿の位置に戻る
		redirectBack(req, res, "post-" + id);
	}
}
