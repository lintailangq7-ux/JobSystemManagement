package DAO;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.GuidanceDetail;
import model.ModelCompany;
import model.ModelStudent;
import model.StudentDetail;
 
public class StudentDetailDAO {
 
    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";
 
    
    public List<StudentDetail> findAllStudentDetail() {
 
        Map<Integer, StudentDetail> studentMap = new LinkedHashMap<>();
        CompanyChukanDAO companyDao = new CompanyChukanDAO();
        EmploymentChukanDAO employmentDao = new EmploymentChukanDAO(); 
        StudentChukanDAO studentDao = new StudentChukanDAO();
 
        String sql =
            "SELECT s.*, j.*, c.* " +
            "FROM 学生テーブル s " +
            "LEFT JOIN 就職情報テーブル j ON s.学籍番号 = j.学籍番号 " +
            "LEFT JOIN 企業テーブル c ON j.企業ID = c.企業ID " +
            "ORDER BY s.学籍番号, j.指導ID";
 
        try(Connection con = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
 
            while(rs.next()) {
                addRowToStudentMap(rs, studentMap, companyDao, employmentDao, studentDao);
            }
 
        } catch(Exception e) {
        	System.out.println(e);
            e.printStackTrace();
        }
 
        return new ArrayList<>(studentMap.values());
    }
 
    /**
     * 企業名（部分一致）で検索し、該当する企業に応募している学生の
     * 学生情報・指導情報・選考履歴を全件取得する。
     *
     * findAllStudentDetail() との違いは、
     *  ・INNER JOIN にしているため「指導情報が無い学生」は結果に含まれない
     *  ・WHERE 企業名 LIKE ? で絞り込んでいる
     * 点のみで、組み立てロジック（addRowToStudentMap）は共通化している。
     *
     * @param keyword 企業名の検索キーワード（部分一致）
     * @return 該当企業に応募している学生ごとの StudentDetail 一覧
     */
    public List<StudentDetail> findByCompanyKeyword(String keyword) {
 
        Map<Integer, StudentDetail> studentMap = new LinkedHashMap<>();
        CompanyChukanDAO companyDao = new CompanyChukanDAO();
        EmploymentChukanDAO employmentDao = new EmploymentChukanDAO();
        StudentChukanDAO studentDao = new StudentChukanDAO();
 
        String sql =
            "SELECT s.*, j.*, c.* " +
            "FROM 学生テーブル s " +
            "INNER JOIN 就職情報テーブル j ON s.学籍番号 = j.学籍番号 " +
            "INNER JOIN 企業テーブル c ON j.企業ID = c.企業ID " +
            "WHERE c.企業名 LIKE ? " +
            "ORDER BY s.学籍番号, j.指導ID";
 
        try(Connection con = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, "%" + keyword + "%");
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addRowToStudentMap(rs, studentMap, companyDao, employmentDao, studentDao);
                }
            }
 
        } catch(Exception e) {
        	System.out.println(e);
            e.printStackTrace();
        }
 
        return new ArrayList<>(studentMap.values());
    }
 
    /**
     * ResultSet の1行分を studentMap に組み立てて追加する共通処理。
     * findAllStudentDetail() と findByCompanyKeyword() の両方から使う。
     */
    private void addRowToStudentMap(ResultSet rs, Map<Integer, StudentDetail> studentMap,
                                     CompanyChukanDAO companyDao, EmploymentChukanDAO employmentDao,
                                     StudentChukanDAO studentDao) throws Exception {
 
        int gakuseki = rs.getInt("学籍番号");
 
        // 生徒がまだ作られていなければ生成
        StudentDetail detail = studentMap.get(gakuseki);
 
        if (detail == null) {
 
            ModelStudent student = new ModelStudent();
            student.setGakusekiNo(gakuseki);
            student.setName(rs.getString("氏名"));
            student.setClassName(rs.getString("クラス"));
            student.setAttendanceNo(rs.getInt("出席番号"));
            student.setZaisekiJokyo(rs.getInt("在籍状況"));
            student.setKenNaiGaiKibo(rs.getString("県内外の希望"));
            student.setSeibetsu(rs.getString("性別"));
            student.setBiko(rs.getString("備考"));
 
            detail = new StudentDetail();
            student.setGakuseiChukanList(studentDao.findById(gakuseki));
            detail.setStudent(student);
            detail.setGuidanceList(new ArrayList<>());
 
            studentMap.put(gakuseki, detail);
        }
 
        // 指導データがある場合だけ追加
        if (rs.getString("指導ID") != null) {
 
            GuidanceDetail guidance = new GuidanceDetail();
 
            guidance.setShidoId(rs.getString("指導ID"));
            guidance.setNaiteiKakutei(rs.getInt("内定確定"));
 
            Timestamp ts = rs.getTimestamp("内定確定日");
            if (ts != null) {
                guidance.setNaiteiKakuteiBi(ts.toLocalDateTime());
            }
 
            guidance.setBiko(rs.getString("備考"));
 
            ModelCompany company = new ModelCompany();
 
            company.setKaishaId(rs.getString("企業ID"));
            company.setKaishaName(rs.getString("企業名"));
            company.setAddress(rs.getString("住所"));
            company.setTel(rs.getString("電話番号"));
            company.setEmail(rs.getString("メールアドレス"));
            company.setSaiyoJisseki(rs.getInt("採用実績"));
            company.setKinmuChi(rs.getString("勤務地"));
 
            guidance.setCompany(company);
            company.setKaishaChukanList(companyDao.findById(company.getKaishaId()));
            guidance.setExamHistory(employmentDao.findById(guidance.getShidoId()));
 
            detail.getGuidanceList().add(guidance);
        }
    }
 
    public StudentDetail findByGakusekiNo(String gakusekiNo) {
 
        ModelStudent student = findStudent(gakusekiNo);
        System.out.println(student + "student");
        if (student == null) return null;
 
        List<GuidanceDetail> guidanceList = findGuidanceList(gakusekiNo);
 
        return new StudentDetail(student, guidanceList);
    }
 
    private ModelStudent findStudent(String gakusekiNo) {
    	StudentChukanDAO scDao = new StudentChukanDAO();
    	System.out.println("findStudent");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("findStudent com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String sql = "SELECT * FROM 学生テーブル WHERE 学籍番号 = ?";
 
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
        	System.out.println("DriverManager.getConnection");
            ps.setString(1, gakusekiNo);
            System.out.println( sql);
            System.out.println(gakusekiNo);
 
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
                    s.setGakuseiChukanList(scDao.findById(rs.getInt("学籍番号")));
                    
                    return s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        	System.out.println(e);
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

	public StudentDetail findByGakusekiNoAndCompanyKeyword(String gakusekiNo, String keyword) {
		// TODO 自動生成されたメソッド・スタブ
		  ModelStudent student = findStudent(gakusekiNo);

		    if (student == null) {
		        return null;
		    }

		    List<GuidanceDetail> guidanceList = new ArrayList<>();

		    String sql =
		        "SELECT j.指導ID, j.内定確定日, j.内定確定, j.備考 AS 指導備考, " +
		        "c.企業ID, c.企業名, c.住所, c.電話番号, c.メールアドレス, c.採用実績, c.勤務地 " +
		        "FROM 就職情報テーブル j " +
		        "INNER JOIN 企業テーブル c ON j.企業ID = c.企業ID " +
		        "WHERE j.学籍番号 = ? " +
		        "AND c.企業名 LIKE ? " +
		        "ORDER BY j.指導ID";


		    try(Connection con = DriverManager.getConnection(URL, USER, PASS);
		        PreparedStatement ps = con.prepareStatement(sql)) {

		        ps.setString(1, gakusekiNo);
		        ps.setString(2, "%" + keyword + "%");


		        ResultSet rs = ps.executeQuery();

		        while(rs.next()) {

		            GuidanceDetail gd = new GuidanceDetail();

		            gd.setShidoId(rs.getString("指導ID"));
		            gd.setNaiteiKakutei(rs.getInt("内定確定"));

		            Timestamp ts = rs.getTimestamp("内定確定日");
		            if(ts != null){
		                gd.setNaiteiKakuteiBi(ts.toLocalDateTime());
		            }

		            gd.setBiko(rs.getString("指導備考"));


		            ModelCompany company = new ModelCompany();

		            company.setKaishaId(rs.getString("企業ID"));
		            company.setKaishaName(rs.getString("企業名"));
		            company.setAddress(rs.getString("住所"));
		            company.setTel(rs.getString("電話番号"));
		            company.setEmail(rs.getString("メールアドレス"));
		            company.setSaiyoJisseki(rs.getInt("採用実績"));
		            company.setKinmuChi(rs.getString("勤務地"));

		            gd.setCompany(company);

		            guidanceList.add(gd);
		        }

		    }catch(Exception e){
		        e.printStackTrace();
		    }

		    
		    return new StudentDetail(student, guidanceList);
		
		
	}

	
}
 