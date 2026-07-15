package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import model.ModelStudent;
import model.StudentChukan;

public class StudentDAO {
	//データベースに接続に使用する情報
	private final String JDBC_URL = "jdbc:mysql://localhost/jop_managment_system";
	private final String DB_USER ="root";
	private final String DB_PASS ="kcsf";
	
	public List<ModelStudent> findAll(){
		 List<ModelStudent> StuList = new ArrayList<>(); 
			StudentChukanDAO StuCukan = new StudentChukanDAO();

			
			InitialContext initCtx;
			DataSource ds =null;
			
			try {
				initCtx = new InitialContext();
				ds =(DataSource)initCtx.lookup("java:comp/env/jdbc/jop_managment_system");
				
			}catch(NamingException e) {
				e.printStackTrace(); 
			}		
			
			try(Connection conn = ds.getConnection()){
				String SQL =("SELECT 学籍番号, クラス, 氏名, 出席番号, 在籍状況, 県内外の希望, 性別, 備考 FROM 学生テーブル;");
				PreparedStatement pStmt = conn.prepareStatement(SQL);
				ResultSet rs = pStmt.executeQuery();
			
				while(rs.next()) {
					int gakusekiNo = rs.getInt("学籍番号");
					String className = rs.getString("クラス");
					String name = rs.getString("氏名");
					int attendanceNo = rs.getInt("出席番号");
					int zaisekiJokyo = rs.getInt("在籍状況");
					String kenNaiGaiKibo = rs.getString("県内外の希望");
					String seibetsu = rs.getString("性別");
					String biko = rs.getString("備考");
					 List<StudentChukan> list = StuCukan.findById(gakusekiNo);
					ModelStudent StuData = new ModelStudent(gakusekiNo,className,name,attendanceNo,zaisekiJokyo,kenNaiGaiKibo,seibetsu,biko,list);
					StuList.add(StuData);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return StuList;
		}
	public boolean create(StudentChukan Sdata) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver") ;
		 } catch (ClassNotFoundException e) {
				throw new IllegalStateException("JDBCドライバを読み込めませんでした");
			}
		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)) {
		
		}catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

