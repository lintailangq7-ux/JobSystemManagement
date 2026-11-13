package saru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import saru.AccountType;
import saru.model.Search;

public class SearchDAO {
	
//	DataSource ds;
//	try {
//		InitialContext ctx = new InitialContext();
//		ds = (DataSource) ctx.lookup("java:comp/env/jdbc/saru_sns");
//	} catch (NamingException e) {
//		e.printStackTrace();
//		return null;
//	}
	
	// ★ 動作確認用の直接接続。通ったら PostDao と同じ接続方法に置き換えｓる
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
	
	public List<Search> findByKeyword(String keyword) {
		List<Search> list = new ArrayList<>();

		// ※ 列名・テーブル名は実際のDBに合わせて調整してください
		String sql = "SELECT p.id, p.userId, p.text, p.imageFile, p.audioFile, "
				+ "p.replyTo, p.createdAt, "
				+ "u.username AS authorUsername, u.displayName AS authorDisplayName, "
				+ "u.accountType AS authorType "
				+ "FROM posts p JOIN users u ON p.userId = u.id "
				+ "WHERE p.text LIKE ? "
				+ "ORDER BY p.createdAt DESC";

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, "%" + keyword + "%");

			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt("id");
					int userId = rs.getInt("user_id");
					String body = rs.getString("body");
					long createdAt = rs.getTimestamp("created_at").getTime();
					String userName = rs.getString("user_name");

					AccountType type = null;
					try {
						type = AccountType.valueOf(rs.getString("account_type").toUpperCase());
					} catch (Exception e) {
						// enumの名前が合わなくても今回は使わないので null のまま
					}

					list.add(new Search(id, userId, body, null, null,
							null, createdAt, userName, userName,
							type, 0, false, 0, null));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
}