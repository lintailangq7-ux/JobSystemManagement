package model;

import java.time.LocalDateTime;

public class EmploymentChukan {

    private int shikenId;                    // ★追加：試験ID（主キー）
    private String shidoId;                  // 指導ID
    private LocalDateTime shikenNichiji;     // 試験日時
    private String shikenNaiyo;              // 試験内容
    private int teishutsuShoruiJokyo;        // 提出書類状況
    private String shikenKaijo;              // 試験会場

    public EmploymentChukan() {}

    public EmploymentChukan(String shidoId, LocalDateTime shikenNichiji, String shikenNaiyo,
                            int teishutsuShoruiJokyo, String shikenKaijo) {
        this.shidoId = shidoId;
        this.shikenNichiji = shikenNichiji;
        this.shikenNaiyo = shikenNaiyo;
        this.teishutsuShoruiJokyo = teishutsuShoruiJokyo;
        this.shikenKaijo = shikenKaijo;
    }

    // ★追加
    public int getShikenId() {
        return shikenId;
    }

    public void setShikenId(int shikenId) {
        this.shikenId = shikenId;
    }

    public String getShidoId() {
        return shidoId;
    }

    public void setShidoId(String shidoId) {
        this.shidoId = shidoId;
    }

    public LocalDateTime getShikenNichiji() {
        return shikenNichiji;
    }

    public void setShikenNichiji(LocalDateTime shikenNichiji) {
        this.shikenNichiji = shikenNichiji;
    }

    public String getShikenNaiyo() {
        return shikenNaiyo;
    }

    public void setShikenNaiyo(String shikenNaiyo) {
        this.shikenNaiyo = shikenNaiyo;
    }

    public int getTeishutsuShoruiJokyo() {
        return teishutsuShoruiJokyo;
    }

    public void setTeishutsuShoruiJokyo(int teishutsuShoruiJokyo) {
        this.teishutsuShoruiJokyo = teishutsuShoruiJokyo;
    }

    public String getShikenKaijo() {
        return shikenKaijo;
    }

    public void setShikenKaijo(String shikenKaijo) {
        this.shikenKaijo = shikenKaijo;
    }
}