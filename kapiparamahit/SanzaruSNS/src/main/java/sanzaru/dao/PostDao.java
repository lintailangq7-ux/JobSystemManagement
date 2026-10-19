package sanzaru.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import sanzaru.AccountType;
import sanzaru.model.Post;

/** posts / likes テーブルの操作 */
public final class PostDao {
	private PostDao() {}

	// 一覧表示に必要な件数などをまとめて取る SELECT (最初の ? は見ている人の id)
	private static final String SELECT = """
			SELECT p.*,
			  a.username AS author_username, a.display_name AS author_display_name, a.type AS author_type,
			  (SELECT COUNT(*) FROM likes WHERE post_id = p.id) AS like_count,
			  EXISTS(SELECT 1 FROM likes WHERE post_id = p.id AND user_id = ?) AS liked,
			  (SELECT COUNT(*) FROM posts r WHERE r.reply_to = p.id) AS reply_count,
			  pa.username AS parent_username
			FROM posts p
			JOIN users a ON a.id = p.user_id
			LEFT JOIN posts pp ON pp.id = p.reply_to
			LEFT JOIN users pa ON pa.id = pp.user_id
			""";

	/** 保存されている画像・音声のファイル情報 (userId は投稿した人) */
	public record Media(String kind, String mime, int userId) {}

	/** 削除した投稿 (返信も含む) */
	public record Deleted(List<Integer> ids, List<String> files) {}

	/** タイムライン。homeOnly なら自分とフォロー中の人だけ */
	public static List<Post> timeline(int viewerId, boolean homeOnly) throws SQLException {
		if (homeOnly) {
			return query(SELECT + """
					WHERE p.user_id = ? OR p.user_id IN (SELECT followee_id FROM follows WHERE follower_id = ?)
					ORDER BY p.created_at DESC, p.id DESC LIMIT 100""", viewerId, viewerId, viewerId);
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

	/** コメント一覧 (古い順) */
	public static List<Post> replies(int viewerId, int postId) throws SQLException {
		return query(SELECT + " WHERE p.reply_to = ? ORDER BY p.created_at, p.id", viewerId, postId);
	}

	/** 投稿者の id (投稿がなければ null) */
	public static Integer ownerOf(int postId) throws SQLException {
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM posts WHERE id = ?")) {
			ps.setInt(1, postId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : null;
			}
		}
	}

	public static int create(int userId, String text, String imageFile, String imageMime, String audioFile,
			String audioMime, Integer replyTo) throws SQLException {
		String sql = "INSERT INTO posts (user_id, text, image_file, image_mime, audio_file, audio_mime, reply_to, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, userId);
			ps.setString(2, text);
			ps.setString(3, imageFile);
			ps.setString(4, imageMime);
			ps.setString(5, audioFile);
			ps.setString(6, audioMime);
			if (replyTo == null) ps.setNull(7, Types.INTEGER);
			else ps.setInt(7, replyTo);
			ps.setLong(8, System.currentTimeMillis());
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				keys.next();
				return keys.getInt(1);
			}
		}
	}

	/** 投稿を削除する。返信(返信の返信も含む)も一緒に削除 */
	public static Deleted delete(int postId) throws SQLException {
		String tree = """
				WITH RECURSIVE tree AS (
				  SELECT id FROM posts WHERE id = ?
				  UNION ALL
				  SELECT p.id FROM posts p JOIN tree t ON p.reply_to = t.id
				)
				SELECT p.id, p.image_file, p.audio_file FROM posts p JOIN tree t ON t.id = p.id""";
		List<Integer> ids = new ArrayList<>();
		List<String> files = new ArrayList<>();
		try (Connection conn = Db.get()) {
			try (PreparedStatement ps = conn.prepareStatement(tree)) {
				ps.setInt(1, postId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						ids.add(rs.getInt("id"));
						if (rs.getString("image_file") != null) files.add(rs.getString("image_file"));
						if (rs.getString("audio_file") != null) files.add(rs.getString("audio_file"));
					}
				}
			}
			if (ids.isEmpty()) return new Deleted(ids, files);
			String in = String.join(",", ids.stream().map(i -> "?").toList());
			try (PreparedStatement ps = conn.prepareStatement("DELETE FROM posts WHERE id IN (" + in + ")")) {
				for (int i = 0; i < ids.size(); i++) ps.setInt(i + 1, ids.get(i));
				ps.executeUpdate();
			}
		}
		return new Deleted(ids, files);
	}

	/** いいねしていれば外し、していなければいいねする */
	public static void toggleLike(int userId, int postId) throws SQLException {
		try (Connection conn = Db.get()) {
			int deleted;
			try (PreparedStatement ps = conn.prepareStatement("DELETE FROM likes WHERE post_id = ? AND user_id = ?")) {
				ps.setInt(1, postId);
				ps.setInt(2, userId);
				deleted = ps.executeUpdate();
			}
			if (deleted == 0) {
				try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO likes (post_id, user_id) VALUES (?, ?)")) {
					ps.setInt(1, postId);
					ps.setInt(2, userId);
					ps.executeUpdate();
				}
			}
		}
	}

	/** ファイル名から、画像か音声か・形式を調べる (投稿に使われていなければ null) */
	public static Media findMedia(String file) throws SQLException {
		String sql = "SELECT user_id, image_file, image_mime, audio_mime FROM posts WHERE image_file = ? OR audio_file = ? LIMIT 1";
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, file);
			ps.setString(2, file);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				return file.equals(rs.getString("image_file"))
						? new Media("image", rs.getString("image_mime"), rs.getInt("user_id"))
						: new Media("audio", rs.getString("audio_mime"), rs.getInt("user_id"));
			}
		}
	}

	private static List<Post> query(String sql, Object... params) throws SQLException {
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
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
		p.setText(rs.getString("text"));
		p.setImageFile(rs.getString("image_file"));
		p.setAudioFile(rs.getString("audio_file"));
		int replyTo = rs.getInt("reply_to");
		p.setReplyTo(rs.wasNull() ? null : replyTo);
		p.setCreatedAt(rs.getLong("created_at"));
		p.setAuthorUsername(rs.getString("author_username"));
		p.setAuthorDisplayName(rs.getString("author_display_name"));
		p.setAuthorType(AccountType.of(rs.getString("author_type")));
		p.setLikeCount(rs.getInt("like_count"));
		p.setLiked(rs.getBoolean("liked"));
		p.setReplyCount(rs.getInt("reply_count"));
		p.setParentUsername(rs.getString("parent_username"));
		return p;
	}
}
