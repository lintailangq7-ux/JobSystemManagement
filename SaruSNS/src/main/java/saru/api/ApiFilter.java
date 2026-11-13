package saru.api;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.UserDao;
import saru.model.User;

/** /api/* の共通処理: CORS の許可 と トークンの確認 */
@WebFilter("/api/*")
public class ApiFilter extends HttpFilter {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		// 開発用: Chrome から呼べるようにする (公開するときは見直す)
		res.setHeader("Access-Control-Allow-Origin", "*");
		res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}

		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (path.equals("/api/ping") || path.equals("/api/login") || path.equals("/api/register")) {
			chain.doFilter(req, res);
			return;
		}

		String header = req.getHeader("Authorization");
		String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;
		Integer userId = ApiTokens.verify(token);
		if (userId == null) {
			ApiJson.error(res, 401, "ログインが必要です");
			return;
		}
		try {
			User me = UserDao.findById(userId, userId);
			if (me == null) {
				ApiJson.error(res, 401, "ログインが必要です");
				return;
			}
			req.setAttribute("apiMe", me);
			req.setAttribute("apiUserId", userId);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		chain.doFilter(req, res);
	}
}