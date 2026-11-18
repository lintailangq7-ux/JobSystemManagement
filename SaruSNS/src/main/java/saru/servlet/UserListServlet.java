package saru.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.UserViewDAO;

/** ユーザー一覧API(確認用)  GET /UserListServlet */
@WebServlet("/UserListServlet")
public class UserListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// ★ Chrome(Flutter Web)から呼べるようにする設定(開発用)
	private static void allowCors(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
		response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		allowCors(response);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		allowCors(response);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		List<Map<String, Object>> users = new UserViewDAO().findAll();
		if (users == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"ユーザーの取得に失敗しました\"}");
			return;
		}
		System.out.println("[UserListServlet] ユーザー " + users.size() + " 件");

		out.print("[");
		for (int i = 0; i < users.size(); i++) {
			if (i > 0) out.print(",");
			out.print("{");
			boolean first = true;
			for (Map.Entry<String, Object> e : users.get(i).entrySet()) {
				if (!first) out.print(",");
				first = false;
				out.print(quote(e.getKey()) + ":" + value(e.getValue()));
			}
			out.print("}");
		}
		out.print("]");
	}

	/** 値をJSONにする(null・数値・文字列) */
	private static String value(Object v) {
		if (v == null) return "null";
		if (v instanceof Number) return v.toString();
		return quote(v.toString());
	}

	/** JSONの文字列にする(" や改行をエスケープ) */
	private static String quote(String v) {
		StringBuilder sb = new StringBuilder("\"");
		for (char c : v.toCharArray()) {
			switch (c) {
			case '"': sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			default:
				if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
				else sb.append(c);
			}
		}
		return sb.append("\"").toString();
	}
}