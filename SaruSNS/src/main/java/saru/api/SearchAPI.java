package saru.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.PostDao;
import saru.model.Post;

/** Flutter用のテキスト検索API  GET /api/search?q=キーワード */
@WebServlet("/api/search")
public class SearchAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_KEYWORD = 100;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Integer userId = (Integer) req.getAttribute("apiUserId");
		String q = req.getParameter("q");
		if (q == null || q.trim().isEmpty()) {
			ApiJson.error(res, 400, "検索キーワードを入力してください");
			return;
		}
		q = q.trim();
		if (q.codePointCount(0, q.length()) > MAX_KEYWORD) {
			ApiJson.error(res, 400, "キーワードは" + MAX_KEYWORD + "文字までです");
			return;
		}
		try {
			String viewerType = PostDao.accountType(userId);
			List<Post> posts = PostDao.searchByText(userId, viewerType, q, 30);
			ApiJson.write(res, 200, ApiJson.posts(posts, userId, viewerType));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}