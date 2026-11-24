package saru.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import saru.AccountType;
import saru.model.Post;

public final class PostDao {
	private PostDao() {}

	private static final String SELECT = """
			SELECT p.id, p.user_id, p.body, p.created_at,
			       u.user_name AS author_user_name,
			       u.user_mail AS author_user_mail,
			       u.account_type AS author_type,
			       (SELECT COUNT(*) FROM likes WHERE post_id = p.id) AS like_count,
			       EXISTS(SELECT 1 FROM likes WHERE post_id = p.id AND user_id = ?) AS liked,
			       (SELECT COUNT(*) FROM comments WHERE post_id = p.id) AS reply_count
			FROM posts p
			JOIN users u ON u.id = p.user_id
			""";

	
	
	public record MediaRow(int id, String type, String fileName) {}

	/** 投稿に付いているメディア(古い順) */
	public static List<MediaRow> mediaOf(int postId) throws SQLException {
		String sql = "SELECT id, type, file_path FROM media WHERE post_id = ? ORDER BY id";
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, postId);
			try (ResultSet rs = ps.executeQuery()) {
				List<MediaRow> list = new ArrayList<>();
				while (rs.next()) list.add(new MediaRow(rs.getInt("id"), rs.getString("type"), rs.getString("file_path")));
				return list;
			}
		}
	}


	/** 見ている人が、見てよい投稿だけにする条件。? は2つ(どちらも見ている人の id) */
	private static final String VISIBLE = """
			(p.visibility = 'public'
			 OR p.user_id = ?
			 OR (p.visibility = 'followers' AND EXISTS (
			       SELECT 1 FROM follows f WHERE f.follower_id = ? AND f.followee_id = p.user_id)))
			""";

	public static List<Post> timeline(int viewerId, boolean homeOnly) throws SQLException {
		if (homeOnly) {
			return query(SELECT + " WHERE " + VISIBLE + """
					AND (p.user_id = ? OR p.user_id IN (
					  SELECT followee_id FROM follows WHERE follower_id = ?))
					ORDER BY p.created_at DESC, p.id DESC LIMIT 100
					""", viewerId, viewerId, viewerId, viewerId, viewerId);
		}
		return query(SELECT + " WHERE " + VISIBLE
				+ " ORDER BY p.created_at DESC, p.id DESC LIMIT 100", viewerId, viewerId, viewerId);
	}

	public static List<Post> byUser(int viewerId, int userId) throws SQLException {
		return query(SELECT + " WHERE p.user_id = ? AND " + VISIBLE
				+ " ORDER BY p.created_at DESC, p.id DESC", viewerId, userId, viewerId, viewerId);
	}

	public static Post findById(int viewerId, int id) throws SQLException {
		List<Post> list = query(SELECT + " WHERE p.id = ? AND " + VISIBLE, viewerId, id, viewerId, viewerId);
		return list.isEmpty() ? null : list.get(0);
	}
	
	public record NewMedia(String type, String fileName, Long dhash, String mfcc) {
		/** 特徴がないメディア(今までの呼び出しは、これで動く) */
		public NewMedia(String type, String fileName) {
			this(type, fileName, null, null);
		}
	}
	
	/** 投稿作成 */
	public static int create(int userId, String body, String visibility, List<NewMedia> media) throws SQLException {
	    try (Connection conn = Db.get()) {
	        conn.setAutoCommit(false);
	        try {
	            int postId;
	            // ✅ visibility をINSERT文から削除！引数はそのまま受け取るだけ
	            try (PreparedStatement ps = conn.prepareStatement(
	                    "INSERT INTO posts (user_id, body) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
	                ps.setInt(1, userId);
	                ps.setString(2, body.isEmpty() ? null : body);
	                // ps.setString(3, visibility); ← 削除！
	                
	                ps.executeUpdate();
	                try (ResultSet keys = ps.getGeneratedKeys()) {
	                    keys.next();
	                    postId = keys.getInt(1);
	                }
	            }
	            if (!media.isEmpty()) {
	            	try (PreparedStatement ins = conn.prepareStatement(
	            			"INSERT INTO media (post_id, type, file_path, dhash) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
	            			PreparedStatement feat = conn.prepareStatement(
	            					"INSERT INTO media_features (media_id, kind, vec) VALUES (?, 'mfcc', ?)")) {
	            		for (NewMedia m : media) {
	            			ins.setInt(1, postId);
	            			ins.setString(2, m.type());
	            			ins.setString(3, m.fileName());
	            			if (m.dhash() == null) ins.setNull(4, Types.BIGINT);
	            			else ins.setLong(4, m.dhash());
	            			ins.executeUpdate();
	            			if (m.mfcc() != null) {
	            				int mediaId;
	            				try (ResultSet keys = ins.getGeneratedKeys()) {
	            					keys.next();
	            					mediaId = keys.getInt(1);
	            				}
	            				feat.setInt(1, mediaId);
	            				feat.setString(2, m.mfcc());
	            				feat.executeUpdate();
	            			}
	            		}
	            	}
	            }
	            conn.commit();
	            return postId;
	        } catch (SQLException | RuntimeException e) {
	            conn.rollback();
	            throw e;
	        } finally {
	            conn.setAutoCommit(true);
	        }
	    }
	}
	

	/** 見ている人が、画像の指紋が近い順に */
	public static List<Post> searchByDHash(int viewerId, long hash, int limit) throws SQLException {
		String sql = SELECT
				+ " JOIN (SELECT post_id, MIN(BIT_COUNT(dhash ^ ?)) AS dist FROM media"
				+ " WHERE type = 'image' AND dhash IS NOT NULL GROUP BY post_id) d ON d.post_id = p.id"
				+ " WHERE " + VISIBLE
				+ " ORDER BY d.dist ASC, p.id DESC LIMIT ?";
		return query(sql, viewerId, hash, viewerId, viewerId, limit);
	}

	public record FeatureRow(int postId, String vec) {}

	/** 見てよい投稿の音声の特徴を、全部読む */
	public static List<FeatureRow> audioFeatures(int viewerId) throws SQLException {
		String sql = "SELECT m.post_id, f.vec FROM media_features f"
				+ " JOIN media m ON m.id = f.media_id JOIN posts p ON p.id = m.post_id"
				+ " WHERE f.kind = 'mfcc' AND " + VISIBLE;
		List<FeatureRow> rows = new ArrayList<>();
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, viewerId);
			ps.setInt(2, viewerId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) rows.add(new FeatureRow(rs.getInt("post_id"), rs.getString("vec")));
			}
		}
		return rows;
	}

	/** id の並びのとおりに、投稿を返す */
	public static List<Post> findInOrder(int viewerId, List<Integer> ids) throws SQLException {
		if (ids.isEmpty()) return new ArrayList<>();
		String marks = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
		List<Object> params = new ArrayList<>();
		params.add(viewerId);
		params.addAll(ids);
		params.add(viewerId);
		params.add(viewerId);
		List<Post> found = query(SELECT + " WHERE p.id IN (" + marks + ") AND " + VISIBLE, params.toArray());
		java.util.Map<Integer, Post> byId = new java.util.HashMap<>();
		for (Post p : found) byId.put(p.getId(), p);
		List<Post> ordered = new ArrayList<>();
		for (int id : ids) {
			Post p = byId.get(id);
			if (p != null) ordered.add(p);
		}
		return ordered;
	}

	/** アカウントの種類 (mizaru / iwazaru / kikazaru) */
	public static String accountType(int userId) throws SQLException {
		try (Connection conn = Db.get();
				PreparedStatement ps = conn.prepareStatement("SELECT account_type FROM users WHERE id = ?")) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString(1) : null;
			}
		}
	}


	/** 投稿削除 */
	public static boolean delete(int postId, int userId) throws SQLException {
		try (Connection conn = Db.get();
				PreparedStatement ps = conn.prepareStatement("DELETE FROM posts WHERE id = ? AND user_id = ?")) {
			ps.setInt(1, postId);
			ps.setInt(2, userId);
			return ps.executeUpdate() > 0;
		}
	}
	
	public record MediaFile(String type, String fileName) {}

	/** 自分の投稿に付いているファイルの名前 (削除の前に、先に控える) */
	public static List<MediaFile> mediaFiles(int postId, int userId) throws SQLException {
		String sql = "SELECT m.type, m.file_path FROM media m JOIN posts p ON p.id = m.post_id"
				+ " WHERE p.id = ? AND p.user_id = ?";
		List<MediaFile> list = new ArrayList<>();
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, postId);
			ps.setInt(2, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) list.add(new MediaFile(rs.getString("type"), rs.getString("file_path")));
			}
		}
		return list;
	}
	
	/** 投稿編集 */
	public static boolean update(int postId, int userId, String newBody) throws SQLException {
	    try (Connection conn = Db.get();
	         PreparedStatement ps = conn.prepareStatement(
	             "UPDATE posts SET body = ? WHERE id = ? AND user_id = ?")) {
	        ps.setString(1, newBody.isEmpty() ? null : newBody);
	        ps.setInt(2, postId);
	        ps.setInt(3, userId);
	        return ps.executeUpdate() > 0;
	    }
	}

	private static List<Post> query(String sql, Object... params) throws SQLException {
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<Post> list = new ArrayList<>();
				while (rs.next()) list.add(map(rs));
				return list;
			}
		}
	}
	
	/** 見てよい投稿のメディアを1件(存在しない、または見てはいけない投稿なら null) */
	public static MediaRow findVisibleMedia(int viewerId, int mediaId) throws SQLException {
		String sql = "SELECT m.id, m.type, m.file_path FROM media m JOIN posts p ON p.id = m.post_id"
				+ " WHERE m.id = ? AND " + VISIBLE;
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, mediaId);
			ps.setInt(2, viewerId);
			ps.setInt(3, viewerId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? new MediaRow(rs.getInt("id"), rs.getString("type"), rs.getString("file_path")) : null;
			}
		}
	}
	
	/** 投稿の本文を見てよいか判定 */
	public static boolean canSeePostText(int viewerId, int postAuthorId, String viewerType) {
	    if (viewerId == postAuthorId) return true;
	    return !"iwazaru".equals(viewerType);
	}


	private static Post map(ResultSet rs) throws SQLException {
	    Post p = new Post();
	    p.setId(rs.getInt("id"));
	    p.setUserId(rs.getInt("user_id"));
	    p.setText(rs.getString("body") == null ? "" : rs.getString("body")); // ✅ null→空文字
	    p.setCreatedAt(rs.getTimestamp("created_at").getTime());
	    p.setAuthorUser_name(rs.getString("author_user_name") == null ? "" : rs.getString("author_user_name"));
	    p.setAuthorUser_mail(rs.getString("author_user_mail") == null ? "" : rs.getString("author_user_mail"));
	    p.setAuthorType(AccountType.of(rs.getString("author_type")));
	    p.setLikeCount(rs.getInt("like_count"));
	    p.setLiked(rs.getBoolean("liked"));
	    p.setReplyCount(rs.getInt("reply_count"));
	    return p;
	}
}