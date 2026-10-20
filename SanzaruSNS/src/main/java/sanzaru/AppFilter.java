package sanzaru;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HexFormat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sanzaru.dao.UserDao;
import sanzaru.model.User;

/**
 * すべてのリクエストの前に動く共通処理
 * - ログイン中のユーザーを request の "me" に入れる
 * - ログインしていなければログイン画面へ
 * - POST のフォームに CSRF 対策のトークンが付いているか確認
 * - 前の画面で出したメッセージ (flash) を受け渡す
 */
@WebFilter("/*")
public class AppFilter extends HttpFilter {
	private static final long serialVersionUID = 1L;
	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (path.startsWith("/css/") || path.startsWith("/js/") || path.equals("/favicon.ico")) {
			chain.doFilter(req, res);
			return;
		}

		String startupError = (String) getServletContext().getAttribute("startupError");
		if (startupError != null) {
			res.setStatus(500);
			req.setAttribute("message", "MySQL に接続できませんでした: " + startupError);
			req.setAttribute("hint", "WEB-INF/db.properties の db.user / db.password などを確認して、Tomcat を再起動してください。");
			req.getRequestDispatcher("/WEB-INF/jsp/message.jsp").forward(req, res);
			return;
		}

		HttpSession session = req.getSession();
		String csrf = (String) session.getAttribute("csrf");
		if (csrf == null) {
			byte[] b = new byte[32];
			RANDOM.nextBytes(b);
			csrf = HexFormat.of().formatHex(b);
			session.setAttribute("csrf", csrf);
		}
		req.setAttribute("csrf", csrf);

		User me = null;
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId != null) {
			try {
				me = UserDao.findById(userId, userId);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
			if (me == null) session.removeAttribute("userId");
		}
		req.setAttribute("me", me);

		if ("POST".equals(req.getMethod())) {
			if (req.getContentType() != null && req.getContentType().startsWith("multipart/")) {
				try {
					req.getParts();
				} catch (IllegalStateException e) {
					// @MultipartConfig の上限を超えた
					session.setAttribute("flashError", "ファイルが大きすぎます (画像は5MB、音声は10MBまで)");
					res.sendRedirect(req.getContextPath() + "/");
					return;
				}
			}
			String token = req.getParameter("csrf");
			if (token == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.US_ASCII),
					csrf.getBytes(StandardCharsets.US_ASCII))) {
				res.sendError(403, "フォームの有効期限が切れました。ページを再読み込みしてからやり直してください");
				return;
			}
		}

		// 投稿を見るにはログインが必要
		if (me == null && !path.equals("/login")) {
			if (path.startsWith("/media/")) res.sendError(404);
			else res.sendRedirect(req.getContextPath() + "/login");
			return;
		}

		if ("GET".equals(req.getMethod()) && !path.startsWith("/media/")) {
			for (String key : new String[] { "flash", "flashError", "reveal" }) {
				Object v = session.getAttribute(key);
				if (v != null) {
					req.setAttribute(key, v);
					session.removeAttribute(key);
				}
			}
		}
		chain.doFilter(req, res);
	}
}
