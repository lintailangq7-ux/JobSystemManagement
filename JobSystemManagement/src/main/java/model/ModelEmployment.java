package model;

import java.time.LocalDateTime;
import java.util.List;

public class ModelEmployment {

    // 指導一覧（メイン情報）
    private String shidoId;           // 指導ID
    private int gakusekiNo;           // 学籍番号
    private String kaishaId;          // 企業ID
    private LocalDateTime naiteiKakuteiBi;   // 内定確定日
    private int naiteiKakutei;        // 内定確定
    private String biko;              // 備考

    // 就職情報中間テーブル（複数）
    private List<EmploymentChukan> shushokuJohoChukanList;

    public ModelEmployment() {}

    // コンストラクタ（必要に応じて拡張）
    public ModelEmployment(String shidoId, int gakusekiNo, String kaishaId,
    		LocalDateTime naiteiKakuteiBi, int naiteiKakutei, String biko,
                           List<EmploymentChukan> shushokuJohoChukanList) {
        this.shidoId = shidoId;
        this.gakusekiNo = gakusekiNo;
        this.kaishaId = kaishaId;
        this.naiteiKakuteiBi = naiteiKakuteiBi;
        this.naiteiKakutei = naiteiKakutei;
        this.biko = biko;
        this.shushokuJohoChukanList = shushokuJohoChukanList;
    }

    // Getter & Setter
    public String getShidoId() { return shidoId; }
    public void setShidoId(String shidoId) { this.shidoId = shidoId; }

    public int getGakusekiNo() { return gakusekiNo; }
    public void setGakusekiNo(int gakusekiNo) { this.gakusekiNo = gakusekiNo; }

    public String getKaishaId() { return kaishaId; }
    public void setKaishaId(String kaishaId) { this.kaishaId = kaishaId; }

    public LocalDateTime getNaiteiKakuteiBi() { return naiteiKakuteiBi; }
    public void setNaiteiKakuteiBi(LocalDateTime naiteiKakuteiBi) { this.naiteiKakuteiBi = naiteiKakuteiBi; }

    public int getNaiteiKakutei() { return naiteiKakutei; }
    public void setNaiteiKakutei(int naiteiKakutei) { this.naiteiKakutei = naiteiKakutei; }

    public String getBiko() { return biko; }
    public void setBiko(String biko) { this.biko = biko; }

    public List<EmploymentChukan> getShushokuJohoChukanList() {
        return shushokuJohoChukanList;
    }

    public void setShushokuJohoChukanList(List<EmploymentChukan> shushokuJohoChukanList) {
        this.shushokuJohoChukanList = shushokuJohoChukanList;
    }
}