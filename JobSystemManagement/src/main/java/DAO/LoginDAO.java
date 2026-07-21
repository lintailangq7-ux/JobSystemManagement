package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import model.Login;


public class LoginDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";

	
    public Login findAll(String gakusekiNo){
	
	
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }
    String sql = "SELECT * FROM login WHERE 学籍番号 = ?;";

    try (Connection con = DriverManager.getConnection(URL, USER, PASS);
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, gakusekiNo);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Login l =  new Login();
                l.setPassword(rs.getString("ユーザーID"));
                l.setUserId(rs.getString("パスワード"));

               
                
                return l;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

}
