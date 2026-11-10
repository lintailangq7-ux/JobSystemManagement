package saru.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import saru.AccountType;
import saru.model.Search;

public class SearchDAO {

	public List<Search> findByKeyword(String keyword) {
		List<Search> list = new ArrayList<>();

		DataSource ds;
		try {
			InitialContext ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/saru_sns");
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}

		// ※ 列名・テーブル名は実際のDBに合わせて調整してください
		String sql = "SELECT p.id, p.userId, p.text, p.imageFile, p.audioFile, "
				+ "p.replyTo, p.createdAt, "
				+ "u.username AS authorUsername, u.displayName AS authorDisplayName, "
				+ "u.accountType AS authorType "
				+ "FROM posts p JOIN users u ON p.userId = u.id "
				+ "WHERE p.text LIKE ? "
				+ "ORDER BY p.createdAt DESC";

		try (Connection conn = ds.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(sql)) {

			pStmt.setString(1, "%" + keyword + "%");

			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt("id");
					int userId = rs.getInt("userId");
					String text = rs.getString("text");
					String imageFile = rs.getString("imageFile");
					String audioFile = rs.getString("audioFile");
					Integer replyTo = rs.getObject("replyTo", Integer.class);
					long createdAt = rs.getLong("createdAt");
					String authorUsername = rs.getString("authorUsername");
					String authorDisplayName = rs.getString("authorDisplayName");
					AccountType authorType = AccountType.valueOf(rs.getString("authorType"));

					// いいね数・返信数・親ユーザー名はここでは仮の値
					list.add(new Search(id, userId, text, imageFile, audioFile,
							replyTo, createdAt, authorUsername, authorDisplayName,
							authorType, 0, false, 0, null));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
}