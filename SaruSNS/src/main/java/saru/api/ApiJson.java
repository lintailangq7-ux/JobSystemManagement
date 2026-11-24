package saru.api;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import saru.dao.PostDao;
import saru.model.Post;
import saru.model.User;

/** API の返事 (JSON) を作る小さな道具 */
public final class ApiJson {
	private ApiJson() {}

	/** 文字列を JSON の文字列にする (" や \ や改行をエスケープ) */
	public static String str(String s) {
		if (s == null) return "null";
		StringBuilder sb = new StringBuilder("\"");
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
					else sb.append(c);
				}
			}
		}
		return sb.append('"').toString();
	}

	public static void write(HttpServletResponse res, int status, String json) throws IOException {
		res.setStatus(status);
		res.setContentType("application/json; charset=UTF-8");
		res.getWriter().write(json);
	}

	public static void error(HttpServletResponse res, int status, String message) throws IOException {
		write(res, status, "{\"error\":" + str(message) + "}");
	}

	public static String user(User u) {
		return "{\"id\":" + u.getId()
				+ ",\"username\":" + str(u.getUser_Mail())
				+ ",\"displayName\":" + str(u.getUser_Name())
				+ ",\"bio\":" + str(u.getBio())
				+ ",\"type\":" + str(u.getType().getKey())
				+ ",\"followingCount\":" + u.getFollowingCount()
				+ ",\"followerCount\":" + u.getFollowerCount()
				+ ",\"postCount\":" + u.getPostCount() + "}";
	}

	/** 一覧。見ている人のアカウントの種類で、制限されたメディアを伏せる */
	public static String posts(List<Post> posts, String viewerType) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < posts.size(); i++) {
			if (i > 0) sb.append(',');
			sb.append(post(posts.get(i), viewerType));
		}
		return sb.append(']').toString();
	}
	
	
	public static String posts(List<Post> posts, int viewerId, String viewerType) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < posts.size(); i++) {
			if (i > 0) sb.append(',');
			sb.append(post(posts.get(i), viewerId, viewerType)); // ✅ 新しいpostメソッドを呼ぶ
		}
		return sb.append(']').toString();
	}


	public static String post(Post p, String viewerType) {
		return "{\"id\":" + p.getId()
				+ ",\"userId\":" + p.getUserId()
				+ ",\"content\":" + str(p.getText())
				+ ",\"text\":" + str(p.getText())
				+ ",\"createdAt\":" + p.getCreatedAt()
				+ ",\"authorName\":" + str(p.getAuthorUser_name())
				+ ",\"authorType\":" + str(p.getAuthorType() == null ? null : p.getAuthorType().getKey())
				+ ",\"likeCount\":" + p.getLikeCount()
				+ ",\"liked\":" + p.isLiked()
				+ ",\"replyCount\":" + p.getReplyCount()
				+ ",\"media\":" + mediaList(p.getId(), viewerType) + "}";
	}

	private static String mediaList(int postId, String viewerType) {
		try {
			List<PostDao.MediaRow> media = PostDao.mediaOf(postId);
			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < media.size(); i++) {
				PostDao.MediaRow m = media.get(i);
				boolean blocked = ("mizaru".equals(viewerType) && "image".equals(m.type()))
						|| ("kikazaru".equals(viewerType) && "audio".equals(m.type()));
				if (i > 0) sb.append(',');
				sb.append("{\"id\":").append(m.id())
						.append(",\"type\":\"").append(m.type()).append("\"")
						.append(",\"blocked\":").append(blocked).append('}');
			}
			return sb.append(']').toString();
		} catch (Exception e) {
			System.err.println("メディアの取得に失敗: " + e.getMessage());
			return "[]";
		}
	}
	
	public static String post(Post p, int viewerId, String viewerType) {
	    boolean canSeeText = PostDao.canSeePostText(viewerId, p.getUserId(), viewerType);
	    String contentJson = canSeeText ? str(p.getText()) : "null";
	    String textHiddenJson = canSeeText ? "false" : "true";
	    
	    return "{\"id\":" + p.getId()
	            + ",\"userId\":" + p.getUserId()
	            + ",\"content\":" + contentJson
	            + ",\"text\":" + contentJson
	            + ",\"textHidden\":" + textHiddenJson
	            + ",\"createdAt\":" + p.getCreatedAt()
	            + ",\"authorName\":" + str(p.getAuthorUser_name())
	            + ",\"authorType\":" + str(p.getAuthorType() == null ? null : p.getAuthorType().getKey())
	            + ",\"likeCount\":" + p.getLikeCount()
	            + ",\"liked\":" + p.isLiked()
	            + ",\"replyCount\":" + p.getReplyCount()
	            + ",\"media\":" + mediaList(p.getId(), viewerType) + "}";
	}

}

