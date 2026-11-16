package saru.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.HistoryDAO;
import saru.model.History;

/**
 * 履歴API  GET /api/history?user_id=1&HistoryMode=1
 * HistoryMode: 閲覧=1  いいね=2  ブックマーク=3
 * CORS・トークン確認は ApiFilter に任せる
 */
@WebServlet("/api/history")
public class HistoryAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		int user_id;
		int HistoryMode;
		try {
			user_id = Integer.parseInt(request.getParameter("user_id"));
			HistoryMode = Integer.parseInt(request.getParameter("HistoryMode"));
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"user_id と HistoryMode を指定してください\"}");
			return;
		}
		if (user_id == 0 || HistoryMode < 1 || HistoryMode > 3) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"user_id または HistoryMode が正しくありません\"}");
			return;
		}

		System.out.println("[HistoryAPI] user_id=" + user_id + " HistoryMode=" + HistoryMode);
		List<History> list = new HistoryDAO().findHistory(user_id, HistoryMode);
		if (list == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"履歴の取得に失敗しました\"}");
			return;
		}

		out.print("[");
		for (int i = 0; i < list.size(); i++) {
			History h = list.get(i);
			if (i > 0) out.print(",");
			out.print("{\"user_id\":" + h.getUser_id()
					+ ",\"post_id\":" + h.getPost_id()
					+ ",\"viewed_at\":"
					+ (h.getViewed_at() == null ? "null" : "\"" + h.getViewed_at() + "\"")
					+ "}");
		}
		out.print("]");
	}
}