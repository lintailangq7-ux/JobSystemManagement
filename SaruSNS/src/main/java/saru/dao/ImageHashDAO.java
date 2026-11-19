package saru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImageHashDAO {
	// ★ 接続の設定は CommentDAO の getConnection() と同じ内容に揃える
	private static final String URL =
			"jdbc:mysql://localhost:3306/saru_sns?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Tokyo";
	private static final String USER = "root";
	private static final String PASS = "KCSF";

	private Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("MySQLドライバが見つかりません(WEB-INF/libを確認)", e);
		}
		return DriverManager.getConnection(URL, USER, PASS);
	}

	/** 投稿の画像の指紋を保存する。成功したら true */
	public boolean save(int postId, long hash) {
		String SQL = "INSERT INTO post_image_hashes (post_id, image_hash) VALUES (?, ?)";
		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {
			pStmt.setInt(1, postId);
			pStmt.setLong(2, hash);
			return pStmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/** 指紋が近い投稿を、近い順に最大30件。失敗したら null */
	public List<Map<String, Object>> searchSimilar(long hash, int maxDistance) {
		List<Map<String, Object>> list = new ArrayList<>();
		String SQL = "SELECT p.id, p.user_id, p.body, p.created_at, u.user_name, "
				+ "MIN(BIT_COUNT(h.image_hash ^ ?)) AS dist "
				+ "FROM post_image_hashes h "
				+ "JOIN posts p ON p.id = h.post_id "
				+ "JOIN users u ON u.id = p.user_id "
				+ "GROUP BY p.id, p.user_id, p.body, p.created_at, u.user_name "
				+ "HAVING dist <= ? "
				+ "ORDER BY dist ASC, p.created_at DESC LIMIT 30";

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {
			pStmt.setLong(1, hash);
			pStmt.setInt(2, maxDistance);
			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> r = new LinkedHashMap<>();
					r.put("id", rs.getInt("id"));
					r.put("user_id", rs.getInt("user_id"));
					r.put("userName", rs.getString("user_name"));
					r.put("body", rs.getString("body"));
					r.put("created_at", rs.getTimestamp("created_at").toLocalDateTime());
					r.put("distance", rs.getInt("dist"));
					list.add(r);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
}