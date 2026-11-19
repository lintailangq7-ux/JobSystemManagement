package saru.api;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import saru.dao.PostDao;
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

	// ✅ 修正：viewerAccountType を受け取ってフィルタする
		private static String mediaList(int postId, String viewerAccountType) {
			try {
				List<PostDao.NewMedia> media = PostDao.getMediaByPostId(postId);
				StringBuilder sb = new StringBuilder("[");
				boolean hasItem = false;
				for (int i = 0; i < media.size(); i++) {
					var m = media.get(i);
					String type = m.type(); // "image" または "audio"
					
					// ========== アカウント種別ごとの表示制御 ==========
					boolean visible = switch (viewerAccountType) {
					case "mizaru" -> !"image".equals(type);     // 見ざる：画像を隠す
				    case "iwazaru" -> true;                       // 言わざる：全部見える
				    case "kikazaru" -> !"audio".equals(type);    // 聞かざる：音声を隠す
						default -> true; // ゲスト：すべて表示
					};
					
					if (visible) {
						if (hasItem) sb.append(',');
						hasItem = true;
						sb.append("{\"type\":\"").append(type).append("\"")
						  .append(",\"fileName\":\"").append(m.fileName()).append("\"")
						  .append("}");
					}
				}
				return sb.append(']').toString();
			} catch (Exception e) {
				return "[]"; // エラー時は空配列
			}
		}
	
	
	
		public static String post(saru.model.Post p, String viewerAccountType) {
		    return "{\"id\":" + p.getId()
		            + ",\"userId\":" + p.getUserId()
		            + ",\"content\":" + str(
		            		 // ========== 表示制御：本文/文字 ==========
		                    "iwazaru".equals(viewerAccountType) ? "" : p.getText()
		              )
		            + ",\"text\":" + str(
		            		// ========== 表示制御：本文/文字（互換用も同じ制御） ==========
		                    "iwazaru".equals(viewerAccountType) ? "" : p.getText()
		              )
		            + ",\"createdAt\":" + p.getCreatedAt()
		            + ",\"authorName\":" + str(p.getAuthorUser_name())
		            + ",\"authorMail\":" + str(p.getAuthorUser_mail())
		            + ",\"authorType\":" + str(p.getAuthorType() == null ? null : p.getAuthorType().getKey())
		            + ",\"likeCount\":" + p.getLikeCount()
		            + ",\"liked\":" + p.isLiked()
		            + ",\"replyCount\":" + p.getReplyCount()
		            + ",\"media\":" + mediaList(p.getId(), viewerAccountType) 
		            + "}";
		}

	public static String posts(java.util.List<saru.model.Post> list, String viewerAccountType) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append(',');
			sb.append(post(list.get(i), viewerAccountType));
		}
		return sb.append(']').toString();
	}
}
