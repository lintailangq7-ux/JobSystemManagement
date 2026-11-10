package saru.api;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

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
}