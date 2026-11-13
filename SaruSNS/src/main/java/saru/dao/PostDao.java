package saru.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

	/** タイムライン（新しい順） */
	public static List<Post> timeline(int viewerId, boolean homeOnly) throws SQLException {
		if (homeOnly) {
			return query(SELECT + """
					WHERE p.user_id = ? OR p.user_id IN (
					  SELECT followee_id FROM follows WHERE follower_id = ?
					)
					ORDER BY p.created_at DESC, p.id DESC LIMIT 100
					""", viewerId, viewerId, viewerId);
		}
		return query(SELECT + " ORDER BY p.created_at DESC, p.id DESC LIMIT 100", viewerId);
	}

	public static List<Post> byUser(int viewerId, int userId) throws SQLException {
		return query(SELECT + " WHERE p.user_id = ? ORDER BY p.created_at DESC, p.id DESC", viewerId, userId);
	}

	public static Post findById(int viewerId, int id) throws SQLException {
		List<Post> list = query(SELECT + " WHERE p.id = ?", viewerId, id);
		return list.isEmpty() ? null : list.get(0);
	}
	
	public record NewMedia(String type, String fileName) {}
	
	/** 投稿作成 */
	public static int create(int userId, String body, String visibility, List<NewMedia> media) throws SQLException {
		try (Connection conn = Db.get()) {
			conn.setAutoCommit(false);
			try {
				int postId;
				try (PreparedStatement ps = conn.prepareStatement(
						"INSERT INTO posts (user_id, body, visibility) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
					ps.setInt(1, userId);
					ps.setString(2, body.isEmpty() ? null : body);
					ps.setString(3, visibility);
					ps.executeUpdate();
					try (ResultSet keys = ps.getGeneratedKeys()) {
						keys.next();
						postId = keys.getInt(1);
					}
				}
				if (!media.isEmpty()) {
					try (PreparedStatement ps = conn.prepareStatement(
							"INSERT INTO media (post_id, type, file_path) VALUES (?, ?, ?)")) {
						for (NewMedia m : media) {
							ps.setInt(1, postId);
							ps.setString(2, m.type());
							ps.setString(3, m.fileName());
							ps.addBatch();
						}
						ps.executeBatch();
					}
				}
				conn.commit();
				return postId;
			} catch (SQLException | RuntimeException e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true); // 接続を使い回す場合に備えて戻す
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

	private static Post map(ResultSet rs) throws SQLException {
		Post p = new Post();
		p.setId(rs.getInt("id"));
		p.setUserId(rs.getInt("user_id"));
		p.setText(rs.getString("body"));
		p.setCreatedAt(rs.getTimestamp("created_at").getTime());
		p.setAuthorUser_name(rs.getString("author_user_name"));
		p.setAuthorUser_mail(rs.getString("author_user_mail"));
		p.setAuthorType(AccountType.of(rs.getString("author_type")));
		p.setLikeCount(rs.getInt("like_count"));
		p.setLiked(rs.getBoolean("liked"));
		p.setReplyCount(rs.getInt("reply_count"));
		return p;
	}
}