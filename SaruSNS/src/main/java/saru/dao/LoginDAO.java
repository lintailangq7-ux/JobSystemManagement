//package saru.dao;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//import javax.naming.InitialContext;
//import javax.sql.DataSource;
//
//import saru.AccountType;
//import saru.model.User;
//
//public class LoginDAO {
//	//データベースに接続に使用する情報
//	private final String JDBC_URL = "jdbc:mysql://localhost/disaster";
//	private final String DB_USER ="root";
//	private final String DB_PASS ="kcsf";
//	
//	public User  findByldAndPass(String username, String bio){
//		User useD = null;
//		InitialContext ctx;
//		DataSource ds =null;
//		
//		try {
//			ctx = new.InitialContext();
//			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/仮");	//仮
//		}catch (NamingException e) {
//			e.printStackTrace();	
//		}
//		
//		try(Connection conn = ds.getConnection()){
//			String SQL = "";
//			PreparedStatement pStmt = conn.prepareStatement(sql);
//			pStmt.setString(1,username);
//			pStmt.setString(2, bio);
//			
//			ResultSet rs = pStmt.executeQuery();	
//			
//			while (rs.next()) {
//				 int id = rs.getInt("");
//				 String username = rs.getString("");
//				 String displayName = rs.getString("");
//				 String bio = rs.getString("");
//				 AccountType type = rs.getAccountType("");
//				 long createdAt = rs.getInt("");
//				 int followingCount = rs.getInt("");
//				 int followerCount = rs.getInt("");
//				 int postCount = rs.getInt("");
//				 boolean followedByViewer = rs.getBoolean("");
//			}	
//		}catch (SQLException e) {
//			e.printStackTrace();
//			return null;
//		}
//		return useD;
//	}
//}
//
////private int id;
////private String username;
////private String displayName;
////private String bio;
////private AccountType type;
////private long createdAt;
////private int followingCount;
////private int followerCount;
////private int postCount;
////private boolean followedByViewer;