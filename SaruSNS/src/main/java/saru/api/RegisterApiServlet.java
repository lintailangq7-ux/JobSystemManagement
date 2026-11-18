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
		
		System.out.println("=== 受信パラメータ ===");
	    System.out.println("email: " + req.getParameter("email"));
	    System.out.println("displayName: " + req.getParameter("displayName"));
	    System.out.println("password: " + req.getParameter("password"));
	    
		
		String userMail = req.getParameter("email") == null ? "" : req.getParameter("email").trim();
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");
		String displayName = req.getParameter("displayName") == null ? "" : req.getParameter("displayName").trim();


		// 簡易バリデーション
		if (userMail.isEmpty() || !userMail.contains("@")) {
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
		    System.out.println("→ DB登録開始: mail=" + userMail + ", name=" + displayName);
		    int userId = UserDao.create(userMail, displayName, AccountType.random(), password);
		    System.out.println("→ 登録成功！userId=" + userId);  // ✅ ここが表示されるか？
		    
		    User me = UserDao.findById(userId, userId);
		    String token = ApiTokens.issue(userId);

		    ApiJson.write(res, 201,
		            "{\"token\":" + ApiJson.str(token) + ",\"user\":" + ApiJson.user(me) + "}");
		} catch (UserDao.DuplicateUsernameException e) {
		    System.out.println("❗ 重複エラー: " + e.getMessage());  // ✅ 重複の場合
		    ApiJson.error(res, 409, "そのメールアドレスは既に登録されています");
		} catch (SQLException e) {
		    System.out.println("❗ SQLエラー: " + e.getMessage());    // ✅ SQLエラーの場合
		    e.printStackTrace();  // 詳細なスタックトレース
		    throw new ServletException(e);
		}
	}
}