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

	// ★ 接続の設定は、動いている他のDAO(PostDao など)と同じ内容に揃える
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

	/** 本文にキーワードを含む投稿を、新しい順に返す。失敗したら null */
	public List<Search> findByKeyword(String keyword) {
		List<Search> list = new ArrayList<>();

		String sql = "SELECT p.id, p.user_id, p.body, p.created_at, "
				+ "u.user_name, u.account_type "
				+ "FROM posts p JOIN users u ON p.user_id = u.id "
				+ "WHERE p.body LIKE ? "
				+ "ORDER BY p.created_at DESC";

		// 入力された % と _ を、ただの文字として探す
		String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, "%" + escaped + "%");

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

					// 画像・音声・返信先・いいね数などは、この検索では使わないので仮の値
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