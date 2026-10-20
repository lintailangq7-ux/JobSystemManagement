package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.PostDao;
import sanzaru.model.Post;

/** 投稿の詳細とコメント一覧 (GET /post?id=1) */
@WebServlet("/post")
public class PostServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		int viewerId = me(req).getId();
		Integer id = parseId(req.getParameter("id"));
		try {
			Post post = id == null ? null : PostDao.findById(viewerId, id);
			if (post == null) {
				notFound(req, res, "投稿が見つかりません");
				return;
			}
			req.setAttribute("post", post);
			req.setAttribute("replies", PostDao.replies(viewerId, post.getId()));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		render(req, res, "post.jsp");
	}
}
