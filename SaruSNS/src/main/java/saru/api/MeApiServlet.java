package saru.api;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.model.User;

/** GET /api/me → ログイン中のユーザー */
@WebServlet("/api/me")
public class MeApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		User me = (User) req.getAttribute("apiMe");   // ApiFilter が入れてくれる
		ApiJson.write(res, 200, ApiJson.user(me));
	}
}