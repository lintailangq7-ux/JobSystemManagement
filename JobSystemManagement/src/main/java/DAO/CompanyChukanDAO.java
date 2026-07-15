package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CompanyChukan;   // 企業モデルクラス

public class CompanyChukanDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";

    /**
     * 企業テーブルから全件を取得
     */
    public List<CompanyChukan> findById(String kaishaId) {
    	
        List<CompanyChukan> list = new ArrayList<>();
        
        String sql = "SELECT * FROM 企業中間 WHERE 企業ID = ?";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                PreparedStatement ps = con.prepareStatement(sql)) {
        	
               ps.setString(1, kaishaId);
               
               try (ResultSet rs = ps.executeQuery()) {
            	   
                   while (rs.next()) {
                       list.add(new CompanyChukan(kaishaId, rs.getString("募集職種")));
                   }
               }
               
  
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("企業データ取得エラー: " + e.getMessage());
        }
        return list;
    }
}



