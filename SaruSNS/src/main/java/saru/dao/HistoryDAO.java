package saru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

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
	public List<History>  findHistory(int user_id,int postTyp) {//後でかきなおして
			History history =null;
			List<History> list = new ArrayList<>();

			DataSource ds;
			try {
				InitialContext ctx = new InitialContext();
				ds = (DataSource) ctx.lookup("java:comp/env/jdbc/saru_sns");
			} catch (NamingException e) {
				e.printStackTrace();
				return null;
			}
			//閲覧=1
			//いいね=2
			//ブックマーク=3
			String SQL = "後で書く";
			if(postTyp ==1) {		//閲覧履歴
				 SQL = "SELECT * FROM view_history"
					   +"WHERE user_id=?";
			}else if(postTyp ==2) { //いいね
				 SQL = "SELECT * FROM likes"
						   +"WHERE user_id=?";
			}else if(postTyp ==3){ //ブックマーク
				 SQL = "SELECT * FROM bookmarks"
						   +"WHERE user_id=?";
			}
			try(Connection conn = ds.getConnection();
				PreparedStatement pStmt = conn.prepareStatement(SQL)){
				pStmt.setInt(1,user_id);
				
				ResultSet rs = pStmt.executeQuery();
				
				while(rs.next()) {
					int post_id = rs.getInt("post_id");
					LocalDateTime viewed_at = rs.getObject("viewed_at", LocalDateTime.class);
					history = new History(user_id,post_id,viewed_at);
				}
				
				
			}catch (SQLException e) {
				e.printStackTrace();
				return null;
			}			
			return list;
	}
	
}

			