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
    // 変更（UPDATE）
    // ==============================
 
    /**
     * 試験情報を1件更新する。
     * 複合主キー（指導ID, 試験日時）で対象行を特定するため、
     * 試験日時そのものを変更する場合は oldShikenNichiji（更新前の日時）を指定する。
     * 試験日時を変更しない場合は、ec.getShikenNichiji() と同じ値を渡せばよい。
     *
     * @param ec               更新後の値（shidoId, 新しいshikenNichiji含む）
     * @param oldShikenNichiji 更新前の試験日時（WHERE句のキーとして使用）
     * @return 更新できたら true
     */
    public boolean update(EmploymentChukan ec) {
 
        String sql = "UPDATE 就職情報中間テーブル "
                   + "SET 試験日時 = ?, 試験内容 = ?, 提出書類状況 = ?, 試験会場 = ? "
                   + "WHERE 指導ID = ?";
 
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            setTimestampOrNull(ps, 1, ec.getShikenNichiji());
            ps.setString(2, ec.getShikenNaiyo());
            ps.setInt(3, ec.getTeishutsuShoruiJokyo());
            ps.setString(4, ec.getShikenKaijo());
            ps.setString(5, ec.getShidoId());

 
            return ps.executeUpdate() > 0;
 
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報更新エラー: " + e.getMessage());
            return false;
        }
    }
 
    // ==============================
    // 削除（DELETE）
    // ==============================
 
    /** 試験情報を1件削除する（指導ID＋試験日時で特定）。 */
    public boolean delete(String shidoId) {
 
        String sql = "DELETE FROM 就職情報中間テーブル WHERE 指導ID = ?";
 
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, shidoId);

 
            return ps.executeUpdate() > 0;
 
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報削除エラー: " + e.getMessage());
            return false;
        }
    }
 
    /**
     * 指定した指導IDの試験情報を全件削除する。
     * 親（就職情報テーブル）の指導情報を削除する前に、
     * 外部キー制約に引っかからないよう先に呼び出す想定。
     */
    public boolean deleteAllByShidoId(String shidoId) {
 
        String sql = "DELETE FROM 就職情報中間テーブル WHERE 指導ID = ?";
 
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, shidoId);
            ps.executeUpdate(); // 0件でも失敗ではないので戻り値は見ない
            return true;
 
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("試験情報一括削除エラー: " + e.getMessage());
            return false;
        }
    }
 
    // ==============================
    // 内部共通処理
    // ==============================
 
    private void setTimestampOrNull(PreparedStatement ps, int index, LocalDateTime value) throws java.sql.SQLException {
        if (value != null) {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        }
    }
}
