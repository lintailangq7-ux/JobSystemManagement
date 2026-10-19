package sanzaru.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.model.User;

/** 各 Servlet で使う共通の処理 */
public abstract class BaseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/** ログイン中のユーザー (AppFilter が入れている) */
	protected static User me(HttpServletRequest req) {
		return (User) req.getAttribute("me");
	}

	/** JSP で画面を表示する。"back" には今の画面の URL を入れておく(フォーム送信後に戻ってくるため) */
	protected static void render(HttpServletRequest req, HttpServletResponse res, String jsp)
			throws ServletException, IOException {
		String back = req.getRequestURI().substring(req.getContextPath().length());
		if (req.getQueryString() != null) back += "?" + req.getQueryString();
		req.setAttribute("back", back);
		req.getRequestDispatcher("/WEB-INF/jsp/" + jsp).forward(req, res);
	}

	protected static void notFound(HttpServletRequest req, HttpServletResponse res, String message)
			throws ServletException, IOException {
		res.setStatus(404);
		req.setAttribute("message", message);
		render(req, res, "message.jsp");
	}

	/** 次の画面で一度だけ表示するメッセージ */
	protected static void flash(HttpServletRequest req, String message) {
		req.getSession().setAttribute("flash", message);
	}

	protected static void flashError(HttpServletRequest req, String message) {
		req.getSession().setAttribute("flashError", message);
	}

	/** フォームの "back" に入っている元の画面に戻る */
	protected static void redirectBack(HttpServletRequest req, HttpServletResponse res, String anchor) throws IOException {
		redirect(req, res, backPath(req) + (anchor == null ? "" : "#" + anchor));
	}

	protected static void redirect(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
		res.sendRedirect(req.getContextPath() + path);
	}

	/** 外部サイトへ飛ばされないように、アプリ内のパスだけを受け付ける */
	protected static String backPath(HttpServletRequest req) {
		String back = req.getParameter("back");
		if (back == null || !back.startsWith("/") || back.startsWith("//") || back.contains("\\")) return "/";
		return back;
	}

	protected static Integer parseId(String s) {
		try {
			return s == null ? null : Integer.valueOf(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
