package saru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import saru.model.Comment;
import saru.model.CommentLike;

public class CommentDAO {
	// ★ 動作確認用の直接接続。通ったら PostDao と同じ接続方法に置き換える
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

	public List<Comment> findCommentList(int Post_id){
		List<Comment> list = new ArrayList<>();

		//コメントを呼び出す
		String SQL = "SELECT id, post_id, user_id, body, created_at "
		           + "FROM comments "
		           + "WHERE post_id = ? "
		           + "ORDER BY created_at ASC";

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {

			pStmt.setInt(1, Post_id);

			try(ResultSet rs = pStmt.executeQuery()){
				while(rs.next()) {
					int id = rs.getInt("id");
					int user_id = rs.getInt("user_id");
					String body = rs.getString("body");
					LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
					Comment comD = new Comment(id,Post_id,user_id,body,createdAt);
					list.add(comD);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	public List<CommentLike> findCommentLikes(int id){
		List<CommentLike> Like_list = new ArrayList<>();

		//コメントのいいねを呼び出す
		String SQL = "SELECT comment_id, user_id, created_at "
		           + "FROM comment_likes "
		           + "WHERE comment_id = ? "
		           + "ORDER BY created_at ASC";

		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {

			pStmt.setInt(1, id);

			try(ResultSet rs = pStmt.executeQuery()){
				while(rs.next()) {
					int user_id = rs.getInt("user_id");
					LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
					CommentLike comLike = new CommentLike(id,user_id,createdAt);
					Like_list.add(comLike);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return Like_list;
	}

	// ★ 追加：コメントしたユーザーの名前を取る(見つからなければ「名無し」)
	//   ※ users の列名が user_name でなければ書き換える
	public String findUserName(int user_id) {
		String SQL = "SELECT user_name FROM users WHERE id = ?";
		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {
			pStmt.setInt(1, user_id);
			try (ResultSet rs = pStmt.executeQuery()) {
				if (rs.next()) return rs.getString("user_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "名無し";
	}

	// ★ 追加：コメントを保存する(成功したら true)
	public boolean addComment(int Post_id, int user_id, String body) {
		String SQL = "INSERT INTO comments (post_id, user_id, body) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)) {
			pStmt.setInt(1, Post_id);
			pStmt.setInt(2, user_id);
			pStmt.setString(3, body);
			return pStmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}