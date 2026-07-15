package model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 1件の指導記録に、紐づく企業情報オブジェクトそのものを持たせたクラス。
 * 企業IDでの突き合わせが不要になる。
 */
public class GuidanceDetail {
    private String shidoId;              // 指導ID
    private LocalDateTime naiteiKakuteiBi;
    private int naiteiKakutei;
    private String biko;

    private ModelCompany company;                 // ← IDではなく企業オブジェクト本体
    private List<EmploymentChukan> examHistory;    // 就職情報中間（選考履歴）

    public GuidanceDetail() {}

    // Getter & Setter
    public String getShidoId() { return shidoId; }
    public void setShidoId(String shidoId) { this.shidoId = shidoId; }

    public LocalDateTime getNaiteiKakuteiBi() { return naiteiKakuteiBi; }
    public void setNaiteiKakuteiBi(LocalDateTime naiteiKakuteiBi) { this.naiteiKakuteiBi = naiteiKakuteiBi; }

    public int getNaiteiKakutei() { return naiteiKakutei; }
    public void setNaiteiKakutei(int naiteiKakutei) { this.naiteiKakutei = naiteiKakutei; }

    public String getBiko() { return biko; }
    public void setBiko(String biko) { this.biko = biko; }

    public ModelCompany getCompany() { return company; }
    public void setCompany(ModelCompany company) { this.company = company; }

    public List<EmploymentChukan> getExamHistory() { return examHistory; }
    public void setExamHistory(List<EmploymentChukan> examHistory) { this.examHistory = examHistory; }

    /** 最新の選考状況を取得するヘルパー */
    public EmploymentChukan getLatestExam() {
        if (examHistory == null || examHistory.isEmpty()) return null;
        return examHistory.get(examHistory.size() - 1);
    }
}
