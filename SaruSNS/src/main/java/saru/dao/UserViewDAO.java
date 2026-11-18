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

/** users テーブルを読むだけのDAO(確認用。パスワードは取り出さない) */
public class UserViewDAO {
	// ★ 動作確認用の直接接続(他のDAOと同じ設定)
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

	/** 全ユーザー(1人 = 列名と値の対応表)。失敗したら null */
	public List<Map<String, Object>> findAll() {
		List<Map<String, Object>> list = new ArrayList<>();
		String SQL = "SELECT id, user_mail, user_name, bio, birthday, icon_path, "
				+ "account_type, created_at FROM users ORDER BY id";

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL);
				ResultSet rs = pStmt.executeQuery()) {
			while (rs.next()) {
				Map<String, Object> u = new LinkedHashMap<>();
				u.put("id", rs.getInt("id"));
				u.put("user_mail", rs.getString("user_mail"));
				u.put("user_name", rs.getString("user_name"));
				u.put("bio", rs.getString("bio"));
				u.put("birthday", rs.getString("birthday"));
				u.put("icon_path", rs.getString("icon_path"));
				u.put("account_type", rs.getString("account_type"));
				u.put("created_at", rs.getString("created_at"));
				list.add(u);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
}