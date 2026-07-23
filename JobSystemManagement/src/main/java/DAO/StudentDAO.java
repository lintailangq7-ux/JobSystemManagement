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
				String SQL =("SELECT 学籍番号, クラス, 氏名, 出席番号, 在籍状況, 県内外の希望, 性別, 備考,あっせん FROM 学生テーブル;");
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
					int assen = rs.getInt("あっせん");
					String biko = rs.getString("備考");
					 List<StudentChukan> list = StuCukan.findById(gakusekiNo);
					ModelStudent StuData = new ModelStudent(gakusekiNo,className,name,attendanceNo,zaisekiJokyo,kenNaiGaiKibo,seibetsu,assen,biko,list);
					StuList.add(StuData);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return StuList;
		}
	
	public boolean create(ModelStudent student) {
	    String sql = "INSERT INTO 学生テーブル "
	               + "(学籍番号, クラス, 氏名, 出席番号, 在籍状況, 県内外の希望, 性別, あっせん, 備考) "
	               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
	         PreparedStatement pStmt = conn.prepareStatement(sql)) {

	        pStmt.setInt(1, student.getGakusekiNo());
	        pStmt.setString(2, student.getClassName());
	        pStmt.setString(3, student.getName());
	        pStmt.setInt(4, student.getAttendanceNo());
	        pStmt.setInt(5, student.getZaisekiJokyo());
	        pStmt.setString(6, student.getKenNaiGaiKibo());
	        pStmt.setString(7, student.getSeibetsu());
	        pStmt.setInt(8, student.getAssen());
	        pStmt.setString(9, student.getBiko());

	        int result = pStmt.executeUpdate();
	        return result > 0;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
//	public boolean create(StudentChukan Sdata) {
//		try {
//			Class.forName("com.mysql.cj.jdbc.Driver") ;
//		 } catch (ClassNotFoundException e) {
//				throw new IllegalStateException("JDBCドライバを読み込めませんでした");
//			}
//		try(Connection conn = DriverManager.getConnection(JDBC_URL,DB_USER,DB_PASS)) {
//		
//		}catch(SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
//	public List<StudentList> findByCompanyId(String companyId) {
//		// TODO 自動生成されたメソッド・スタブ
//		 List<StudentList> studentList = new ArrayList<>();
//
//		    String sql ="SELECT クラス番号, 出席番号, 氏名 FROM 学生テーブル WHERE 企業ID = ?";
//
//		    try {  Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
//		        PreparedStatement ps = conn.prepareStatement(sql);
//
//		        ps.setString(1, companyId);
//
//		        ResultSet rs = ps.executeQuery();
//
//		        while (rs.next()) {
//
//		            StudentList student = new StudentList();
//
//		            student.setClassNo(
//		                rs.getString("クラス番号")
//		            );
//
//		            student.setAttendanceNo(
//		                rs.getString("出席番号")
//		            );
//
//		            student.setStudentName(
//		                rs.getString("氏名")
//		            );
//
//		            studentList.add(student);
//		        }
//
//		    } catch(Exception e) {
//		        e.printStackTrace();
//		    }
//
//		    return studentList;
//		
//	}


	public String findCompanyName(String companyId) {
		// TODO 自動生成されたメソッド・スタブ
		 String companyName = null;

		    String sql =
		            "SELECT 企業名 "
		          + "FROM 企業テーブル "
		          + "WHERE 企業ID = ?";


		    try (

		        Connection conn =
		                DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);


		        PreparedStatement ps =
		                conn.prepareStatement(sql);){


		        ps.setString(1, companyId);


		        ResultSet rs =
		                ps.executeQuery();


		        if (rs.next()) {

		            companyName =
		                    rs.getString("企業名");

		        }

		    
		    } catch (Exception e) {

		        e.printStackTrace();

		    }


		    return companyName;
	}
}

