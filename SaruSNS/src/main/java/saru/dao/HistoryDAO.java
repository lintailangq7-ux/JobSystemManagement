package saru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import saru.model.History;

public class HistoryDAO {
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

	// 閲覧=1  いいね=2  ブックマーク=3
	// 日時の列は、閲覧は viewed_at、いいね・ブックマークは created_at なので、
	// どれも viewed_at という名前に揃えて取り出す
	public List<History> findHistory(int user_id, int postTyp) {
		List<History> list = new ArrayList<>();

		String SQL;
		if (postTyp == 1) { // 閲覧履歴
			SQL = "SELECT post_id, viewed_at FROM view_history "
					+ "WHERE user_id = ? ORDER BY viewed_at DESC";
		} else if (postTyp == 2) { // いいね
			SQL = "SELECT post_id, created_at AS viewed_at FROM likes "
					+ "WHERE user_id = ? ORDER BY created_at DESC";
		} else if (postTyp == 3) { // ブックマーク
			SQL = "SELECT post_id, created_at AS viewed_at FROM bookmarks "
					+ "WHERE user_id = ? ORDER BY created_at DESC";
		} else {
			return null; // 種類が不正
		}

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {
			pStmt.setInt(1, user_id);

			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					int post_id = rs.getInt("post_id");
					LocalDateTime viewed_at = rs.getObject("viewed_at", LocalDateTime.class);
					History history = new History(user_id, post_id, viewed_at);
					list.add(history);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
}