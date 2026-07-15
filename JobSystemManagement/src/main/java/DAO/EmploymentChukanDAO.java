package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.EmploymentChukan;

public class EmploymentChukanDAO {
	
    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";
    
    public List<EmploymentChukan> findById(String shidoId) {
    	
        List<EmploymentChukan> list = new ArrayList<>();
        
        String sql = "SELECT * FROM 就職情報中間テーブル WHERE 指導ID = ? ";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                PreparedStatement ps = con.prepareStatement(sql)) {
        	
               ps.setString(1, shidoId);
               
               try (ResultSet rs = ps.executeQuery()) {
                   while (rs.next()) {
                       EmploymentChukan ec = new EmploymentChukan();
                       ec.setShidoId(shidoId);
                       Timestamp ts = rs.getTimestamp("試験日時");
                       if (ts != null) ec.setShikenNichiji(ts.toLocalDateTime());
                       ec.setShikenNaiyo(rs.getString("試験内容"));
                       ec.setTeishutsuShoruiJokyo(rs.getInt("提出書類状況"));
                       ec.setShikenKaijo(rs.getString("試験会場"));
                       list.add(ec);
                   }
               }
     
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("企業データ取得エラー: " + e.getMessage());
        }
        return list;
    }
}
