package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.ModelEmployment;

public class EmploymentDAO {   // 指導＋就職情報

    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";

    /**
     * 就職情報（指導含む）を全件取得
     */
    public List<ModelEmployment> findAll() {
        List<ModelEmployment> list = new ArrayList<>();
        EmploymentChukanDAO EmploymentChukanDAO = new EmploymentChukanDAO();

        String sql = "SELECT * FROM 就職情報テーブル ORDER BY 指導ID";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                	ModelEmployment e = new ModelEmployment();

                    e.setShidoId(rs.getString("指導ID"));
                    e.setGakusekiNo(rs.getInt("学籍番号"));
                    e.setKaishaId(rs.getString("企業ID"));
                    Timestamp ts = rs.getTimestamp("内定確定日");
                    if (ts != null) {
                        e.setNaiteiKakuteiBi(ts.toLocalDateTime());
                    }
                    e.setNaiteiKakutei(rs.getInt("内定確定"));
                    e.setBiko(rs.getString("備考"));
                    e.setShushokuJohoChukanList(EmploymentChukanDAO.findById(rs.getString("指導ID")));

                    list.add(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("就職情報取得エラー: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 指定した学籍番号の指導情報（就職情報）を取得
     * @param gakusekiBango 学籍番号
     * @return 就職情報リスト（1人の学生が複数の企業に応募している場合を考慮）
     */
 
}
