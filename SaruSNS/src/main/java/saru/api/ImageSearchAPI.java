package saru.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
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

/**
 * 画像検索API(dHash)  POST /api/image-search  (multipart: image=画像ファイル)
 * ログイン確認は ApiFilter に任せる
 */
@WebServlet("/api/image-search")
@MultipartConfig(maxFileSize = 4 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024) // 画像は4MBまで
public class ImageSearchAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// 指紋の違いがこの数以下なら「似ている」(0=同じ画像。大きくするほど広く拾う)
	private static final int MAX_DISTANCE = 10;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

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
		System.out.println("[ImageSearchAPI] hash=" + Long.toHexString(hash));

		// 指紋が近い投稿を探す
		List<Map<String, Object>> results = new ImageHashDAO().searchSimilar(hash, MAX_DISTANCE);
		if (results == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"検索に失敗しました\"}");
			return;
		}

		// JSON: { "results":[{id,user_id,userName,body,timeAgo,distance,similarity}] }
		out.print("{\"results\":[");
		for (int i = 0; i < results.size(); i++) {
			Map<String, Object> r = results.get(i);
			int distance = (Integer) r.get("distance");
			if (i > 0) out.print(",");
			out.print("{\"id\":" + r.get("id")
					+ ",\"user_id\":" + r.get("user_id")
					+ ",\"userName\":" + quote((String) r.get("userName"))
					+ ",\"body\":" + quote((String) r.get("body"))
					+ ",\"timeAgo\":" + quote(timeAgo((LocalDateTime) r.get("created_at")))
					+ ",\"distance\":" + distance
					+ ",\"similarity\":" + Math.round((64 - distance) * 100.0 / 64) + "}");
		}
		out.print("]}");
	}

	private static String timeAgo(LocalDateTime t) {
		if (t == null) return "";
		long s = Duration.between(t, LocalDateTime.now()).getSeconds();
		if (s < 60) return "たった今";
		if (s < 3600) return (s / 60) + "分前";
		if (s < 86400) return (s / 3600) + "時間前";
		return (s / 86400) + "日前";
	}

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