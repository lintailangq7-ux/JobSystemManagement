package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.AccountType;
import sanzaru.Passwords;
import sanzaru.dao.UserDao;

/** ログイン / 新規登録 (mode で切り替える) */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (me(req) != null) {
			redirect(req, res, "/");
			return;
		}
		req.setAttribute("mode", "register".equals(req.getParameter("mode")) ? "register" : "login");
		render(req, res, "login.jsp");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		boolean register = "register".equals(req.getParameter("mode"));
		String username = trim(req.getParameter("username"));
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");
		String displayName = trim(req.getParameter("displayName"));
		try {
			int userId;
			if (register) {
				if (!username.matches("[a-zA-Z0-9_]{3,20}")) {
					showError(req, res, "register", "ユーザー名は英数字と_で3〜20文字にしてください");
					return;
				}
				if (password.length() < 8) {
					showError(req, res, "register", "パスワードは8文字以上にしてください");
					return;
				}
				String salt = Passwords.newSalt();
				String name = displayName.isEmpty() ? username : cut(displayName, 50);
				try {
					userId = UserDao.create(username, name, AccountType.random(), salt, Passwords.hash(password, salt));
				} catch (UserDao.DuplicateUsernameException e) {
					showError(req, res, "register", "そのユーザー名は既に使われています");
					return;
				}
				// 登録直後にどのタイプになったか発表する
				req.getSession().setAttribute("reveal", true);
			} else {
				UserDao.Credentials c = UserDao.findCredentials(username);
				if (c == null || !Passwords.verify(password, c.salt(), c.passwordHash())) {
					showError(req, res, "login", "ユーザー名またはパスワードが違います");
					return;
				}
				userId = c.id();
				flash(req, "ログインしました");
			}
			req.changeSessionId(); // セッション固定攻撃の対策
			req.getSession().setAttribute("userId", userId);
			redirect(req, res, "/");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private static void showError(HttpServletRequest req, HttpServletResponse res, String mode, String message)
			throws ServletException, IOException {
		res.setStatus(400);
		req.setAttribute("mode", mode);
		req.setAttribute("error", message);
		req.setAttribute("username", req.getParameter("username"));
		req.setAttribute("displayName", req.getParameter("displayName"));
		render(req, res, "login.jsp");
	}

	static String trim(String s) {
		return s == null ? "" : s.trim();
	}

	/** 文字数 (絵文字も1文字) で切る */
	static String cut(String s, int max) {
		return s.codePointCount(0, s.length()) <= max ? s : s.substring(0, s.offsetByCodePoints(0, max));
	}
}
