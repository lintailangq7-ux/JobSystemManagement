package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.GuidanceDetail;
import model.ModelCompany;
import model.ModelStudent;
import model.StudentDetail;

public class StudentDetailDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";

    public StudentDetail findByGakusekiNo(String gakusekiNo) {

        ModelStudent student = findStudent(gakusekiNo);
        if (student == null) return null;

        List<GuidanceDetail> guidanceList = findGuidanceList(gakusekiNo);

        return new StudentDetail(student, guidanceList);
    }

    private ModelStudent findStudent(String gakusekiNo) {
    	
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String sql = "SELECT * FROM 学生テーブル WHERE 学籍番号 = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, gakusekiNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ModelStudent s = new ModelStudent();
                    s.setGakusekiNo(rs.getInt("学籍番号"));
                    s.setClassName(rs.getString("クラス"));
                    s.setName(rs.getString("氏名"));
                    s.setAttendanceNo(rs.getInt("出席番号"));
                    s.setZaisekiJokyo(rs.getInt("在籍状況"));
                    s.setKenNaiGaiKibo(rs.getString("県内外の希望")); // ← カラム名修正
                    s.setSeibetsu(rs.getString("性別"));
                    s.setBiko(rs.getString("備考"));
                    return s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<GuidanceDetail> findGuidanceList(String gakusekiNo) {
        List<GuidanceDetail> list = new ArrayList<>();
        CompanyChukanDAO  CompanyChukanDAO  = new CompanyChukanDAO();
        EmploymentChukanDAO EmploymentChukanDAO = new EmploymentChukanDAO();
        
        // 指導 + 企業 を1回のJOINで取得（企業中間は別途IDで取りに行く）
        String sql =
            "SELECT j.指導ID, j.内定確定日, j.内定確定, j.備考 AS 指導備考, " +
            "       c.企業ID, c.企業名, c.住所, c.電話番号, c.メールアドレス, c.採用実績, c.勤務地 " +
            "FROM 就職情報テーブル j " +
            "INNER JOIN 企業テーブル c ON j.企業ID = c.企業ID " +
            "WHERE j.学籍番号 = ? " +
            "ORDER BY j.指導ID";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, gakusekiNo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GuidanceDetail gd = new GuidanceDetail();
                    gd.setShidoId(rs.getString("指導ID"));

                    Timestamp ts = rs.getTimestamp("内定確定日");
                    if (ts != null) gd.setNaiteiKakuteiBi(ts.toLocalDateTime());
                    gd.setNaiteiKakutei(rs.getInt("内定確定"));
                    gd.setBiko(rs.getString("指導備考"));

                    ModelCompany company = new ModelCompany();
                    String kaishaId = rs.getString("企業ID");
                    company.setKaishaId(kaishaId);
                    company.setKaishaName(rs.getString("企業名"));
                    company.setAddress(rs.getString("住所"));
                    company.setTel(rs.getString("電話番号"));
                    company.setEmail(rs.getString("メールアドレス"));
                    company.setSaiyoJisseki(rs.getInt("採用実績"));
                    company.setKinmuChi(rs.getString("勤務地"));
                    company.setKaishaChukanList(CompanyChukanDAO.findById(kaishaId));
                    gd.setCompany(company);

                    gd.setExamHistory(EmploymentChukanDAO.findById(gd.getShidoId()));

                    list.add(gd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

   
}