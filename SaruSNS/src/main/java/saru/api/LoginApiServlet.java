package saru.api;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.UserDao;
import saru.model.User;

/** POST /api/login  (username, password) → { token, user } */
@WebServlet("/api/login")
public class LoginApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String userMail = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");

		try {
			UserDao.Credentials c = UserDao.findCredentials(userMail);
			if (c == null || !password.equals(c.password())) {
				ApiJson.error(res, 401, "メールアドレスまたはパスワードが違います");
				return;
			}
			User me = UserDao.findById(c.id(), c.id());
			String token = ApiTokens.issue(c.id());
			ApiJson.write(res, 200, "{\"token\":" + ApiJson.str(token) + ",\"user\":" + ApiJson.user(me) + "}");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}