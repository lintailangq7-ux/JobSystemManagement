package sanzaru.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sanzaru.AccountType;
import sanzaru.model.User;

/** users / follows テーブルの操作 */
public final class UserDao {
	private UserDao() {}

	// 一覧表示に必要な件数などをまとめて取る SELECT (最初の ? は見ている人の id)
	private static final String SELECT = """
			SELECT u.*,
			  (SELECT COUNT(*) FROM follows WHERE follower_id = u.id) AS following_count,
			  (SELECT COUNT(*) FROM follows WHERE followee_id = u.id) AS follower_count,
			  (SELECT COUNT(*) FROM posts WHERE user_id = u.id) AS post_count,
			  EXISTS(SELECT 1 FROM follows WHERE follower_id = ? AND followee_id = u.id) AS followed
			FROM users u
			""";

	/** ログイン確認用 (ソルトとハッシュ) */
	public record Credentials(int id, String salt, String passwordHash) {}

	/** ユーザー名の重複 */
	public static class DuplicateUsernameException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static User findById(int viewerId, int id) throws SQLException {
		List<User> list = query(SELECT + " WHERE u.id = ?", viewerId, id);
		return list.isEmpty() ? null : list.get(0);
	}

	/** ユーザー名で探す (大文字小文字は区別しない) */
	public static User findByUsername(int viewerId, String username) throws SQLException {
		List<User> list = query(SELECT + " WHERE u.username = ?", viewerId, username);
		return list.isEmpty() ? null : list.get(0);
	}

	public static List<User> findAll(int viewerId) throws SQLException {
		return query(SELECT + " ORDER BY u.created_at DESC, u.id DESC", viewerId);
	}

	public static Credentials findCredentials(String username) throws SQLException {
		try (Connection conn = Db.get();
				PreparedStatement ps = conn.prepareStatement("SELECT id, salt, password_hash FROM users WHERE username = ?")) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? new Credentials(rs.getInt("id"), rs.getString("salt"), rs.getString("password_hash")) : null;
			}
		}
	}

	/** 新しいユーザーを登録して id を返す */
	public static int create(String username, String displayName, AccountType type, String salt, String passwordHash)
			throws SQLException, DuplicateUsernameException {
		String sql = "INSERT INTO users (username, display_name, bio, type, salt, password_hash, created_at) VALUES (?, ?, '', ?, ?, ?, ?)";
		try (Connection conn = Db.get();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, username);
			ps.setString(2, displayName);
			ps.setString(3, type.getKey());
			ps.setString(4, salt);
			ps.setString(5, passwordHash);
			ps.setLong(6, System.currentTimeMillis());
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				keys.next();
				return keys.getInt(1);
			}
		} catch (SQLIntegrityConstraintViolationException e) {
			// username 列は大文字小文字を区別しない照合順序なので、UNIQUE で重複を防げる
			throw new DuplicateUsernameException();
		}
	}

	public static void updateProfile(int id, String displayName, String bio) throws SQLException {
		try (Connection conn = Db.get();
				PreparedStatement ps = conn.prepareStatement("UPDATE users SET display_name = ?, bio = ? WHERE id = ?")) {
			ps.setString(1, displayName);
			ps.setString(2, bio);
			ps.setInt(3, id);
			ps.executeUpdate();
		}
	}

	/** フォローしていれば外し、していなければフォローする */
	public static void toggleFollow(int followerId, int followeeId) throws SQLException {
		try (Connection conn = Db.get()) {
			int deleted;
			try (PreparedStatement ps = conn.prepareStatement("DELETE FROM follows WHERE follower_id = ? AND followee_id = ?")) {
				ps.setInt(1, followerId);
				ps.setInt(2, followeeId);
				deleted = ps.executeUpdate();
			}
			if (deleted == 0) {
				try (PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO follows (follower_id, followee_id) VALUES (?, ?)")) {
					ps.setInt(1, followerId);
					ps.setInt(2, followeeId);
					ps.executeUpdate();
				}
			}
		}
	}

	private static List<User> query(String sql, Object... params) throws SQLException {
		try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
			try (ResultSet rs = ps.executeQuery()) {
				List<User> list = new ArrayList<>();
				while (rs.next()) list.add(map(rs));
				return list;
			}
		}
	}

	private static User map(ResultSet rs) throws SQLException {
		User u = new User();
		u.setId(rs.getInt("id"));
		u.setUsername(rs.getString("username"));
		u.setDisplayName(rs.getString("display_name"));
		u.setBio(rs.getString("bio"));
		u.setType(AccountType.of(rs.getString("type")));
		u.setCreatedAt(rs.getLong("created_at"));
		u.setFollowingCount(rs.getInt("following_count"));
		u.setFollowerCount(rs.getInt("follower_count"));
		u.setPostCount(rs.getInt("post_count"));
		u.setFollowedByViewer(rs.getBoolean("followed"));
		return u;
	}
}
