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
    

        // ==============================
        // 追加（INSERT）
        // ==============================

        /**
         * 指導情報を新規追加する。指導IDは自動採番（G001, G002, ... の形式）。
         *
         * @param gakusekiNo      学籍番号
         * @param kigyoId         企業ID
         * @param naiteiKakuteiBi 内定確定日（未定の場合は null）
         * @param naiteiKakutei   内定確定フラグ（0 or 1）
         * @param biko            備考
         * @return 発行された指導ID。失敗した場合は null。
         */
        public String insertGuidance(int gakusekiNo, String kigyoId,
                                      LocalDateTime naiteiKakuteiBi, int naiteiKakutei, String biko) {

            String sql = "INSERT INTO 就職情報テーブル "
                       + "(指導ID, 学籍番号, 企業ID, 内定確定日, 内定確定, 備考) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

                String newId = generateNewShidoId(con);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, newId);
                    ps.setInt(2, gakusekiNo);
                    ps.setString(3, kigyoId);

                    if (naiteiKakuteiBi != null) {
                        ps.setTimestamp(4, Timestamp.valueOf(naiteiKakuteiBi));
                    } else {
                        ps.setNull(4, java.sql.Types.TIMESTAMP);
                    }

                    ps.setInt(5, naiteiKakutei);
                    ps.setString(6, biko);

                    int result = ps.executeUpdate();
                    return (result > 0) ? newId : null;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
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
    }

