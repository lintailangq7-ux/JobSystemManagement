package saru.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.SearchDAO;
import saru.model.Search;
//s
/** Flutter用のテキスト検索API  GET /api/search?q=キーワード */
@WebServlet("/api/search")
public class SearchAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String q = request.getParameter("q");
		if (q == null || q.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print("{\"error\":\"検索キーワードを入力してください\"}");
			return;
		}

		List<Search> list = new SearchDAO().findByKeyword(q.trim());
		if (list == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print("{\"error\":\"検索に失敗しました\"}");
			return;
		}

		PrintWriter out = response.getWriter();
		out.print("[");
		for (int i = 0; i < list.size(); i++) {
			Search s = list.get(i);
			if (i > 0) out.print(",");
			out.print("{\"id\":" + s.getId()
					+ ",\"userId\":" + s.getUserId()
					+ ",\"userName\":" + quote(s.getAuthorUsername())
					+ ",\"body\":" + quote(s.getText())
					+ ",\"createdAt\":" + s.getCreatedAt()
					+ ",\"timeAgo\":" + quote(s.getTimeAgo()) + "}");
		}
		out.print("]");
	}

	/** JSONの文字列にする(" や改行をエスケープ) */
	private static String quote(String v) {
		if (v == null) return "null";
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