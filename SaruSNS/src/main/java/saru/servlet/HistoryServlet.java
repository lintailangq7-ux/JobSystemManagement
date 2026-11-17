package saru.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import saru.dao.HistoryDAO;
import saru.model.History;

/**
 * 履歴API  /HistoryServlet?user_id=1&HistoryMode=1
 * HistoryMode: 閲覧=1  いいね=2  ブックマーク=3
 */
@WebServlet("/HistoryServlet")
public class HistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handle(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handle(request, response);
	}
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}
	private void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*"); // ★ 追加(開発用)
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		HistoryDAO DAO = new HistoryDAO();
		List<History> list = new ArrayList<>();
		int user_id;
		int HistoryMode;
		try {
			user_id = Integer.parseInt(request.getParameter("user_id"));
			HistoryMode = Integer.parseInt(request.getParameter("HistoryMode"));
		} catch (NumberFormatException e) {
			System.out.println("[HistoryServlet] user_id または HistoryMode が数値ではありません");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"user_id と HistoryMode を指定してください\"}");
			return;
		}
		HttpSession session = request.getSession(); // 仮

		if (user_id != 0 && HistoryMode >= 1 && HistoryMode <= 3) {
			System.out.println("[HistoryServlet] user_id=" + user_id + " HistoryMode=" + HistoryMode);
			list = DAO.findHistory(user_id, HistoryMode);
			if (list == null) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				out.print("{\"error\":\"履歴の取得に失敗しました\"}");
				return;
			}
			session.setAttribute("History", list);
		} else {
			System.out.println("エラー");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"user_id または HistoryMode が正しくありません\"}");
			return;
		}

		// JSONで返す(キー名は History モデルの項目名のまま)
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