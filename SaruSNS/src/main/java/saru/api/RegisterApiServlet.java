package saru.api;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.AccountType;
import saru.dao.UserDao;
import saru.model.User;

/** POST /api/register  (username, password, displayName) → { token, user } */
@WebServlet("/api/register")
public class RegisterApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String userMail = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");
		String displayName = req.getParameter("displayName") == null ? "" : req.getParameter("displayName").trim();

		// 簡易バリデーション
		if (userMail.isEmpty() ) {
			ApiJson.error(res, 400, "正しいメールアドレスを入力してください");
			return;
		}
		if (password.length() < 8) {
			ApiJson.error(res, 400, "パスワードは8文字以上にしてください");
			return;
		}
		if (displayName.isEmpty()) {
			displayName = userMail.split("@")[0]; // 空ならメールの@前を使う
		}

		try {
			int userId = UserDao.create(userMail, displayName, AccountType.random(), password);
			User me = UserDao.findById(userId, userId);
			String token = ApiTokens.issue(userId);

			ApiJson.write(res, 201,
					"{\"token\":" + ApiJson.str(token) + ",\"user\":" + ApiJson.user(me) + "}");
		} catch (UserDao.DuplicateUsernameException e) {
			ApiJson.error(res, 409, "そのメールアドレスは既に登録されています");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}