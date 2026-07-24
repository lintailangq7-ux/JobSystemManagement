package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.StudentChukan;

public class StudentChukanDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";

    /**
     * 学籍番号をキーに、希望職種（中間テーブル）を全件取得
     */
    public List<StudentChukan> findById(int gakusekiNo) {
        List<StudentChukan> list = new ArrayList<>();
        String sql = "SELECT * FROM 学生中間 WHERE 学籍番号 = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gakusekiNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentChukan c = new StudentChukan();
                    c.setGakusekiNo(rs.getInt("学籍番号"));
                    c.setKibouShokushu(rs.getString("希望職種"));
                    list.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("学生中間データ取得エラー: " + e.getMessage());
        }
        return list;
    }

    /**
     * 該当学籍番号の希望職種を全削除
     */
    public void deleteByGakusekiNo(int gakusekiNo) {
        String sql = "DELETE FROM 学生中間 WHERE 学籍番号 = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gakusekiNo);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("学生中間データ削除エラー: " + e.getMessage());
        }
    }

    /**
     * 希望職種を1件追加
     */
    public void insert(int gakusekiNo, String kibouShokushu) {
        if (kibouShokushu == null || kibouShokushu.isEmpty()) {
            return; // 空欄の希望職種は登録しない
        }
        String sql = "INSERT INTO 学生中間 (学籍番号, 希望職種) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gakusekiNo);
            ps.setString(2, kibouShokushu);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("学生中間データ登録エラー: " + e.getMessage());
        }
    }

    /**
     * 希望職種を一括更新（既存削除 → 新規登録）
     */
    public void updateAll(int gakusekiNo, List<String> kibouShokushuList) {
        deleteByGakusekiNo(gakusekiNo);
        for (String kibou : kibouShokushuList) {
            insert(gakusekiNo, kibou);
        }
    }
}