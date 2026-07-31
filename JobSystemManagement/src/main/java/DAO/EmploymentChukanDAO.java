package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.EmploymentChukan;

public class EmploymentChukanDAO {

    private static final String URL  = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";

    // ==============================
    // 指導IDで全件取得
    // ==============================
    public List<EmploymentChukan> findById(String shidoId) {
        List<EmploymentChukan> list = new ArrayList<>();
        String sql = "SELECT * FROM 就職情報中間テーブル WHERE 指導ID = ? ORDER BY 指導ID";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, shidoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmploymentChukan ec = new EmploymentChukan();
                    ec.setShikenId(rs.getInt("試験ID"));          // ★追加
                    ec.setShidoId(rs.getString("指導ID"));
                    Timestamp ts = rs.getTimestamp("試験日時");
                    if (ts != null) {
                        ec.setShikenNichiji(ts.toLocalDateTime());
                    }
                    ec.setShikenNaiyo(rs.getString("試験内容"));
                    ec.setTeishutsuShoruiJokyo(rs.getInt("提出書類状況"));
                    ec.setShikenKaijo(rs.getString("試験会場"));
                    list.add(ec);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報取得エラー: " + e.getMessage());
        }
        return list;
    }

    // ==============================
    // 新規追加
    // ==============================
    public boolean insert(EmploymentChukan ec) {
        String sql = "INSERT INTO 就職情報中間テーブル "
                   + "(指導ID, 試験日時, 試験内容, 提出書類状況, 試験会場) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ec.getShidoId());
            setTimestampOrNull(ps, 2, ec.getShikenNichiji());
            ps.setString(3, ec.getShikenNaiyo());
            ps.setInt(4, ec.getTeishutsuShoruiJokyo());
            ps.setString(5, ec.getShikenKaijo());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報追加エラー: " + e.getMessage());
            return false;
        }
    }

    // ==============================
    // 試験IDで更新（推奨）
    // ==============================
    public boolean updateById(EmploymentChukan ec) {

        String sql = "UPDATE 就職情報中間テーブル "
                   + "SET 試験日時 = ?, 試験内容 = ?, 提出書類状況 = ?, 試験会場 = ? "
                   + "WHERE 試験ID = ?";

        try(Connection con = DriverManager.getConnection(URL, USER, PASS);
            PreparedStatement ps = con.prepareStatement(sql)) {

            setTimestampOrNull(ps, 1, ec.getShikenNichiji());
            ps.setString(2, ec.getShikenNaiyo());
            ps.setInt(3, ec.getTeishutsuShoruiJokyo());
            ps.setString(4, ec.getShikenKaijo());
            ps.setInt(5, ec.getShikenId());

            return ps.executeUpdate() > 0;

        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // 試験IDで削除
    // ==============================
    public boolean deleteById(int shikenId) {
        String sql = "DELETE FROM 就職情報中間テーブル WHERE 指導ID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, shikenId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報削除エラー: " + e.getMessage());
            return false;
        }
    }

    // ==============================
    // 指導IDで全件削除
    // ==============================
    public boolean deleteAllByShidoId(String shidoId) {
    	String sql = "DELETE FROM 就職情報中間テーブル WHERE 試験ID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, shidoId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報一括削除エラー: " + e.getMessage());
            return false;
        }
    }

    // ==============================
    // 旧update（互換用・非推奨）
    // ==============================
    public boolean update(EmploymentChukan ec, LocalDateTime oldShikenNichiji) {
        // 試験IDがある場合は updateById を使う
        if (ec.getShikenId() > 0) {
            return updateById(ec);
        }
        // 従来の日時キー方式（非推奨）
        String sql = "UPDATE 就職情報中間テーブル "
                   + "SET 試験日時 = ?, 試験内容 = ?, 提出書類状況 = ?, 試験会場 = ? "
                   + "WHERE 指導ID = ? AND 試験日時 = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
            setTimestampOrNull(ps, 1, ec.getShikenNichiji());
            ps.setString(2, ec.getShikenNaiyo());
            ps.setInt(3, ec.getTeishutsuShoruiJokyo());
            ps.setString(4, ec.getShikenKaijo());
            ps.setString(5, ec.getShidoId());
            setTimestampOrNull(ps, 6, oldShikenNichiji);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==============================
    // 内部共通
    // ==============================
    private void setTimestampOrNull(PreparedStatement ps, int index, LocalDateTime value)
            throws java.sql.SQLException {
        if (value != null) {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        }
    }
}