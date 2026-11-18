package saru.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.CommentDAO;
import saru.model.Comment;
import saru.model.CommentLike;

/**
 * コメントAPI
 *  GET  /api/comment?Post_id=1&user_id=1   コメント一覧(user_id は省略可)
 *  POST /api/comment  Post_id, user_id, body   コメントを保存
 * ログイン確認は ApiFilter に任せる
 */
@WebServlet("/api/comment")
public class CommentAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// ★ Chrome(Flutter Web)から呼べるようにする設定(開発用。setHeader なので重複しない)
	private static void allowCors(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		allowCors(response);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	// ---------- コメント一覧 ----------
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		allowCors(response);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		int Post_id;
		try {
			Post_id = Integer.parseInt(request.getParameter("Post_id"));
		} catch (NumberFormatException e) {
			Post_id = 0;
		}
		if (Post_id == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"Post_id が正しくありません\"}");
			return;
		}

		// 見ている人のID(省略したら 0 = 誰もいいねしていない扱い)
		int viewerId;
		try {
			viewerId = Integer.parseInt(request.getParameter("user_id"));
		} catch (NumberFormatException e) {
			viewerId = 0;
		}

		System.out.println("[CommentAPI] コメント一覧 Post_id=" + Post_id + " viewer=" + viewerId);
		CommentDAO DAO = new CommentDAO();
		List<Comment> commentList = DAO.findCommentList(Post_id);
		if (commentList == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"コメントの取得に失敗しました\"}");
			return;
		}

		Map<Integer, String> names = new HashMap<>(); // 同じ人の名前を何度も調べない
		out.print("[");
		for (int i = 0; i < commentList.size(); i++) {
			Comment c = commentList.get(i);
			if (i > 0) out.print(",");

			String name = names.get(c.getUser_id());
			if (name == null) {
				name = DAO.findUserName(c.getUser_id());
				names.put(c.getUser_id(), name);
			}

			// いいねの数と、見ている人がいいね済みか
			int likeCount = 0;
			boolean liked = false;
			List<CommentLike> likes = DAO.findCommentLikes(c.getId());
			if (likes != null) {
				likeCount = likes.size();
				for (CommentLike l : likes) {
					if (l.getUser_id() == viewerId) liked = true;
				}
			}

			out.print("{\"id\":" + c.getId()
					+ ",\"post_id\":" + c.getPost_id()
					+ ",\"user_id\":" + c.getUser_id()
					+ ",\"userName\":" + quote(name)
					+ ",\"body\":" + quote(c.getBody())
					+ ",\"created_at\":" + quote(String.valueOf(c.getCreated_at()))
					+ ",\"timeAgo\":" + quote(timeAgo(c.getCreated_at()))
					+ ",\"likeCount\":" + likeCount
					+ ",\"liked\":" + liked + "}");
		}
		out.print("]");
	}

	// ---------- コメントを保存 ----------
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		allowCors(response);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		int Post_id;
		int user_id;
		try {
			Post_id = Integer.parseInt(request.getParameter("Post_id"));
			user_id = Integer.parseInt(request.getParameter("user_id"));
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"Post_id と user_id を指定してください\"}");
			return;
		}
		String body = request.getParameter("body");
		if (body == null || body.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"コメントを入力してください\"}");
			return;
		}
		body = body.trim();
		if (body.length() > 500) { // comments.body は varchar(500)
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"コメントは500文字までです\"}");
			return;
		}

		System.out.println("[CommentAPI] コメント保存 Post_id=" + Post_id + " user_id=" + user_id);
		if (new CommentDAO().addComment(Post_id, user_id, body)) {
			response.setStatus(HttpServletResponse.SC_CREATED);
			out.print("{\"ok\":true}");
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print("{\"error\":\"コメントを保存できませんでした\"}");
		}
	}

	/** 「3分前」のような表示 */
	private static String timeAgo(LocalDateTime t) {
		if (t == null) return "";
		long s = Duration.between(t, LocalDateTime.now()).getSeconds();
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