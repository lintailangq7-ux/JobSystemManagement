package saru.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import saru.dao.ImageHashDAO;
import saru.dao.SearchDAO;
import saru.model.Search;

/**
 * 検索API(ログイン確認は ApiFilter に任せる)
 *  GET  /api/search?q=キーワード                  テキスト検索   (SearchMode=1)
 *  POST /api/search  multipart: image=画像, SearchMode=2   画像検索(dHash)
 *  SearchMode=3(音声)はこれから実装
 */
@WebServlet("/api/search")
@MultipartConfig(maxFileSize = 4 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024) // 画像は4MBまで
public class SearchAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 検索方法
	private static final int MODE_TEXT = 1;
	private static final int MODE_IMAGE = 2;
	private static final int MODE_VOICE = 3;

	// 画像検索：指紋の違いがこの数以下なら「似ている」(0=同じ画像)
	private static final int MAX_DISTANCE = 10;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		dispatch(request, response, MODE_TEXT); // GET の既定はテキスト検索
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		dispatch(request, response, MODE_IMAGE); // POST の既定は画像検索
	}

	// ---------- 検索方法を見て振り分ける ----------
	private void dispatch(HttpServletRequest request, HttpServletResponse response, int defaultMode)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		// 検索方法 1=テキスト 2=画像 3=音声(指定が無い・不正な値なら既定)
		int searchMode;
		try {
			searchMode = Integer.parseInt(request.getParameter("SearchMode"));
		} catch (NumberFormatException e) {
			searchMode = defaultMode;
		}

		if (searchMode == MODE_TEXT) {
			System.out.println("[SearchAPI] テキスト検索モードです (SearchMode=" + searchMode + ")");
			textSearch(request, response, out);

		} else if (searchMode == MODE_IMAGE) {
			System.out.println("[SearchAPI] 画像検索モードです (SearchMode=" + searchMode + ")");
			if (!"POST".equals(request.getMethod())) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print("{\"error\":\"画像検索は、画像を POST で送ってください\"}");
				return;
			}
			imageSearch(request, response, out);

		} else if (searchMode == MODE_VOICE) { // これから実装
			System.out.println("[SearchAPI] 音声検索モードです(未実装)");
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			out.print("{\"error\":\"音声検索はまだ使えません\"}");

		} else {
			System.out.println("[SearchAPI] 不明な検索モードです (SearchMode=" + searchMode + ")");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"検索方法が正しくありません\"}");
		}
	}

	// ---------- テキスト検索 ----------
	private void textSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		String q = request.getParameter("q");
		if (q == null) q = request.getParameter("SearchText"); // 古い名前でも受ける
		if (q == null || q.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"検索キーワードを入力してください\"}");
			return;
		}
		q = q.trim();
		System.out.println("[SearchAPI] キーワード=" + q);

		List<Search> list = new SearchDAO().findByKeyword(q);
		if (list == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"検索に失敗しました\"}");
			return;
		}

		out.print("[");
		for (int i = 0; i < list.size(); i++) {
			Search s = list.get(i);
			if (i > 0) out.print(",");
			writePost(out, s.getId(), s.getUserId(), s.getAuthorUsername(), s.getText(),
					s.getCreatedAt(), s.getTimeAgo(), null);
		}
		out.print("]");
	}

	// ---------- 画像検索(dHash) ----------
	private void imageSearch(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
			throws ServletException, IOException {
		// 画像を受け取る
		byte[] bytes;
		try {
			Part part = request.getPart("image");
			if (part == null || part.getSize() == 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print("{\"error\":\"画像が送られていません\"}");
				return;
			}
			bytes = part.getInputStream().readAllBytes();
		} catch (IllegalStateException e) { // 大きすぎるとき
			response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			out.print("{\"error\":\"画像は4MBまでです\"}");
			return;
		}

		// 画像の指紋を作る
		long hash;
		try {
			hash = DHash.compute(bytes);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"この形式の画像は使えません(jpg / png / gif / bmp)\"}");
			return;
		}
		System.out.println("[SearchAPI] hash=" + Long.toHexString(hash));

		// 指紋が近い投稿を探す
		List<Map<String, Object>> results = new ImageHashDAO().searchSimilar(hash, MAX_DISTANCE);
		if (results == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"検索に失敗しました\"}");
			return;
		}

		out.print("[");
		for (int i = 0; i < results.size(); i++) {
			Map<String, Object> r = results.get(i);
			int distance = (Integer) r.get("distance");
			long createdAt = ((LocalDateTime) r.get("created_at"))
					.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			if (i > 0) out.print(",");
			writePost(out, (Integer) r.get("id"), (Integer) r.get("user_id"),
					(String) r.get("userName"), (String) r.get("body"),
					createdAt, timeAgo(createdAt),
					Math.round((64 - distance) * 100.0 / 64));
		}
		out.print("]");
	}

	// ---------- 投稿1件をJSONで書く(similarity は画像検索のときだけ) ----------
	private static void writePost(PrintWriter out, int id, int userId, String userName, String body,
			long createdAt, String timeAgo, Long similarity) {
		out.print("{\"id\":" + id
				+ ",\"userId\":" + userId
				+ ",\"userName\":" + quote(userName)
				+ ",\"body\":" + quote(body)
				+ ",\"createdAt\":" + createdAt
				+ ",\"timeAgo\":" + quote(timeAgo)
				+ (similarity == null ? "" : ",\"similarity\":" + similarity)
				+ "}");
	}

	/** 「3分前」のような表示 */
	private static String timeAgo(long createdAtMillis) {
		long s = (System.currentTimeMillis() - createdAtMillis) / 1000;
		if (s < 60) return "たった今";
		if (s < 3600) return (s / 60) + "分前";
		if (s < 86400) return (s / 3600) + "時間前";
		return (s / 86400) + "日前";
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