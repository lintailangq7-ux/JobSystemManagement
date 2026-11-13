package saru.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 検索方法
	private static final int MODE_TEXT = 1;
	private static final int MODE_IMAGE = 2;
	private static final int MODE_VOICE = 3;

	// Flutter(GET)でも、フォーム(POST)でも同じ処理にする
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

	private void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// 検索方法 1=テキスト 2=画像 3=音声(指定が無い・不正な値ならテキスト扱い)
		String keyword = request.getParameter("SearchText"); // 仮
		int searchMode;
		try {
			searchMode = Integer.parseInt(request.getParameter("SearchMode")); // 仮
		} catch (NumberFormatException e) {
			searchMode = MODE_TEXT;
		}

		PrintWriter out = response.getWriter();

		if (searchMode == MODE_TEXT) { // テキスト
			System.out.println("[SearchServlet] テキスト検索モードです (SearchMode=" + searchMode
					+ ", SearchText=" + keyword + ")");

			if (keyword == null || keyword.trim().isEmpty()) {
				System.out.println("[SearchServlet] 検索キーワードが空です");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print("{\"error\":\"検索キーワードを入力してください\"}");
				return;
			}
			keyword = keyword.trim();

			// ★ 仮の検索結果(あとでDAOの検索に置き換える)
			out.print("[");
			for (int i = 1; i <= 3; i++) {
				if (i > 1) out.print(",");
				out.print("{\"id\":" + i
						+ ",\"userId\":" + i
						+ ",\"userName\":" + quote("sample" + i)
						+ ",\"body\":" + quote("「" + keyword + "」の仮の検索結果 " + i)
						+ ",\"createdAt\":" + System.currentTimeMillis()
						+ ",\"timeAgo\":" + quote("たった今") + "}");
			}
			out.print("]");

		} else if (searchMode == MODE_IMAGE) { // 画像(これから実装)
			System.out.println("[SearchServlet] 画像検索モードです(未実装)");
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			out.print("{\"error\":\"画像検索はまだ使えません\"}");

		} else if (searchMode == MODE_VOICE) { // 音声(これから実装)
			System.out.println("[SearchServlet] 音声検索モードです(未実装)");
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			out.print("{\"error\":\"音声検索はまだ使えません\"}");

		} else {
			System.out.println("[SearchServlet] 不明な検索モードです (SearchMode=" + searchMode + ")");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"検索方法が正しくありません\"}");
		}
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