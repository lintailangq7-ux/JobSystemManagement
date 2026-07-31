package DAO;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.EmploymentChukan;
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
     * 指導IDを指定して、就職情報（指導）を1件取得する。
     * 変更画面（EmploymentCangeServlet）で、既存データをフォームに
     * 表示するために追加。
     *
     * @param shidoId 指導ID
     * @return 該当する ModelEmployment。存在しない場合は null。
     */
    public ModelEmployment findById(String shidoId) {
 
        if (shidoId == null || shidoId.isEmpty()) {
            return null;
        }
 
        EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();
        String sql = "SELECT * FROM 就職情報テーブル WHERE 指導ID = ?";
 
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, shidoId);
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                    e.setShushokuJohoChukanList(ecDAO.findById(shidoId));
 
                    return e;
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("就職情報取得エラー(findById): " + e.getMessage());
        }
 
        return null;
    }
 
 
        // ==============================
        // 追加（INSERT）
        // ==============================
 
        /**
         * 指導情報を新規追加する。指導IDは自動採番（G001, G002, ... の形式）。
         * 試験情報は1件のみ登録する旧バージョン（互換用に残置）。
         * 複数件登録する場合は insertGuidanceWithExams を使用すること。
         *
         * @param gakusekiNo      学籍番号
         * @param kigyoId         企業ID
         * @param naiteiKakuteiBi 内定確定日（未定の場合は null）
         * @param naiteiKakutei   内定確定フラグ（0 or 1）
         * @param biko            備考
         * @return 発行された指導ID。失敗した場合は null。
         */
    public String insertGuidanceWithExam(String gakusekiNo, String kigyoId,
            LocalDateTime naiteiKakuteiBi, int naiteiKakutei, String biko,
            EmploymentChukan chukan) {
 
        List<EmploymentChukan> list = new ArrayList<>();
        if (chukan != null) {
            list.add(chukan);
        }
        return insertGuidanceWithExams(gakusekiNo, kigyoId, naiteiKakuteiBi, naiteiKakutei, biko, list);
    }
 
    /**
     * 指導情報を新規追加し、あわせて複数件の試験情報（就職情報中間テーブル）を
     * 同一トランザクションでまとめて登録する。
     * 指導IDの採番、就職情報テーブルへのINSERT、試験情報の一括INSERTが
     * すべて成功した場合のみコミットする。
     *
     * @param gakusekiNo      学籍番号
     * @param kigyoId         企業ID
     * @param naiteiKakuteiBi 内定確定日（未定の場合は null）
     * @param naiteiKakutei   内定確定フラグ（0 or 1）
     * @param biko            備考
     * @param examList        登録する試験情報のリスト（0件でも可）
     * @return 発行された指導ID。失敗した場合は null。
     */
    public String insertGuidanceWithExams(String gakusekiNo, String kigyoId,
            LocalDateTime naiteiKakuteiBi, int naiteiKakutei, String biko,
            List<EmploymentChukan> examList) {
 
        Connection con = null;
        try {
            con = DriverManager.getConnection(URL, USER, PASS);
            con.setAutoCommit(false);
 
            String newId = generateNewShidoId(con);
 
            try (PreparedStatement ps1 = con.prepareStatement(
                    "INSERT INTO 就職情報テーブル (指導ID,学籍番号,企業ID,内定確定日,内定確定,備考) VALUES (?,?,?,?,?,?)")) {
                ps1.setString(1, newId);
                ps1.setString(2, gakusekiNo);
                ps1.setString(3, kigyoId);
                if (naiteiKakuteiBi != null) ps1.setTimestamp(4, Timestamp.valueOf(naiteiKakuteiBi));
                else ps1.setNull(4, java.sql.Types.TIMESTAMP);
                ps1.setInt(5, naiteiKakutei);
                ps1.setString(6, biko);
                ps1.executeUpdate();
            }
 
            if (examList != null && !examList.isEmpty()) {
                try (PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO 就職情報中間テーブル (指導ID,試験日時,試験内容,提出書類状況,試験会場) VALUES (?,?,?,?,?)")) {
 
                    for (EmploymentChukan chukan : examList) {
                        ps2.setString(1, newId);
                        if (chukan.getShikenNichiji() != null) ps2.setTimestamp(2, Timestamp.valueOf(chukan.getShikenNichiji()));
                        else ps2.setNull(2, java.sql.Types.TIMESTAMP);
                        ps2.setString(3, chukan.getShikenNaiyo());
                        ps2.setInt(4, chukan.getTeishutsuShoruiJokyo());
                        ps2.setString(5, chukan.getShikenKaijo());
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            }
 
            con.commit();  // すべて成功して初めて確定
            return newId;
 
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return null; // 呼び出し元は「失敗した」と正しく認識できる
 
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
 
        /**
         * 現在登録されている指導IDの最大値から、次の指導ID（G001形式）を採番する。
         * 呼び出し元と同じコネクション上で実行することで、採番と登録の間の
         * 競合をできる限り避ける。
         */
        private String generateNewShidoId(Connection con) throws SQLException {
            String sql = "SELECT 指導ID FROM 就職情報テーブル ORDER BY 指導ID DESC LIMIT 1";
 
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
 
                int nextNumber = 1;
 
                if (rs.next()) {
                    String lastId = rs.getString("指導ID"); // 例: "G008"
                    String numberPart = lastId.replaceAll("[^0-9]", ""); // "008"
                    if (!numberPart.isEmpty()) {
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    }
                }
 
                return String.format("G%03d", nextNumber); // G009 のような形式
            }
        }
 
        // ==============================
        // 変更（UPDATE）
        // ==============================
 
        /**
         * 指導IDを指定して、指導情報を更新する。
         *
         * @param shidoId         更新対象の指導ID（変更不可のキー）
         * @param kigyoId         企業ID
         * @param naiteiKakuteiBi 内定確定日（未定の場合は null）
         * @param naiteiKakutei   内定確定フラグ（0 or 1）
         * @param biko            備考
         * @return 更新できたら true
         */
        public boolean updateGuidance(String shidoId, String kigyoId,
                LocalDateTime naiteiKakuteiBi, int naiteiKakutei, String biko) {
 
        		String sql = "UPDATE 就職情報テーブル "
        					+ "SET 企業ID = ?, 内定確定日 = ?, 内定確定 = ?, 備考 = ? "
        					+ "WHERE 指導ID = ?";
 
        		try (Connection con = DriverManager.getConnection(URL, USER, PASS);
        				PreparedStatement ps = con.prepareStatement(sql)) {
 
        			ps.setString(1, kigyoId);
 
        			if (naiteiKakuteiBi != null) {
        				ps.setTimestamp(2, Timestamp.valueOf(naiteiKakuteiBi));
        			} else {
        				ps.setNull(2, java.sql.Types.TIMESTAMP);
        			}
 
        			ps.setInt(3, naiteiKakutei);
        			ps.setString(4, biko);
        			ps.setString(5, shidoId);
 
        			return ps.executeUpdate() > 0;
 
        		} catch (SQLException e) {
        			e.printStackTrace();
        			System.out.println("指導情報更新エラー: " + e.getMessage());
        			return false;
        		}
        }
 
        /**
         * 内定確定フラグ・内定確定日だけをピンポイントで更新したい場合の簡易メソッド。
         * （選考状況の更新画面などで使う想定）
         */
        public boolean updateNaiteiStatus(String shidoId, int naiteiKakutei, LocalDateTime naiteiKakuteiBi) {
 
            String sql = "UPDATE 就職情報テーブル SET 内定確定 = ?, 内定確定日 = ? WHERE 指導ID = ?";
 
            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql)) {
 
                ps.setInt(1, naiteiKakutei);
 
                if (naiteiKakuteiBi != null) {
                    ps.setTimestamp(2, Timestamp.valueOf(naiteiKakuteiBi));
                } else {
                    ps.setNull(2, java.sql.Types.TIMESTAMP);
                }
 
                ps.setString(3, shidoId);
 
                return ps.executeUpdate() > 0;
 
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
 
        // ==============================
        // 削除（DELETE）
        // ==============================
 
        /**
         * 指導情報を削除する。
         * 就職情報中間テーブル（試験情報）が指導IDを外部キー参照しているため、
         * 同一トランザクションで「子（中間テーブル） → 親（就職情報テーブル）」の順に削除する。
         *
         * @param shidoId 削除対象の指導ID
         * @return 削除できたら true
         */
        public boolean deleteGuidance(String shidoId) {
 
            String deleteChildSql  = "DELETE FROM 就職情報中間テーブル WHERE 指導ID = ?";
            String deleteParentSql = "DELETE FROM 就職情報テーブル WHERE 指導ID = ?";
 
            Connection con = null;
 
            try {
                con = DriverManager.getConnection(URL, USER, PASS);
                con.setAutoCommit(false); // トランザクション開始
 
                try (PreparedStatement psChild = con.prepareStatement(deleteChildSql)) {
                    psChild.setString(1, shidoId);
                    psChild.executeUpdate();
                }
 
                int affected;
                try (PreparedStatement psParent = con.prepareStatement(deleteParentSql)) {
                    psParent.setString(1, shidoId);
                    affected = psParent.executeUpdate();
                }
 
                con.commit();
                return affected > 0;
 
            } catch (SQLException e) {
                e.printStackTrace();
                if (con != null) {
                    try {
                        con.rollback(); // 失敗したら子テーブルの削除も取り消す
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                return false;
 
            } finally {
                if (con != null) {
                    try {
                        con.setAutoCommit(true);
                        con.close();
                    } catch (SQLException closeEx) {
                        closeEx.printStackTrace();
                    }
                }
            }
            
            
        }	
        public String insertGuidance(
                String gakusekiNo,
                String kigyoId,
                LocalDateTime naiteiKakuteiBi,
                int naiteiKakutei,
                String biko) {
 
 
            try(Connection con =
                DriverManager.getConnection(URL,USER,PASS)){
 
 
                String newId = generateNewShidoId(con);
 
 
                String sql =
                    "INSERT INTO 就職情報テーブル "
                  + "(指導ID,学籍番号,企業ID,内定確定日,内定確定,備考)"
                  + " VALUES(?,?,?,?,?,?)";
 
 
                try(PreparedStatement ps =
                    con.prepareStatement(sql)){
 
 
                    ps.setString(1,newId);
                    ps.setString(2,gakusekiNo);
                    ps.setString(3,kigyoId);
 
 
                    if(naiteiKakuteiBi != null){
 
                        ps.setTimestamp(
                            4,
                            Timestamp.valueOf(naiteiKakuteiBi)
                        );
 
                    }else{
 
                        ps.setNull(
                            4,
                            java.sql.Types.TIMESTAMP
                        );
                    }
 
 
                    ps.setInt(5,naiteiKakutei);
                    ps.setString(6,biko);
 
 
                    ps.executeUpdate();
 
 
                    return newId;
                }
 
 
            }catch(Exception e){
 
                e.printStackTrace();
                return null;
            }
        }
        }