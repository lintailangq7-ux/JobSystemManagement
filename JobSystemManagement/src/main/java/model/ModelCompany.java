package model;

//2. Kaisha.java（企業テーブル）
import java.util.List;

public class ModelCompany {

    // 企業メイン情報
    private String kaishaId;      // 企業ID
    private String kaishaName;    // 企業名
    private String address;       // 住所
    private String tel;           // 電話番号
    private String email;         // メールアドレス
    private int saiyoJisseki;     // 採用実績
    private String kinmuChi;      // 勤務地

    // 企業中間テーブル（募集職種：複数）
    private List<CompanyChukan> CompanyChukanList;

    public ModelCompany() {}

    public ModelCompany(String kaishaId, String kaishaName, String address, String tel,
                       String email, int saiyoJisseki, String kinmuChi,
                       List<CompanyChukan> kaishaChukanList) {
        this.kaishaId = kaishaId;
        this.kaishaName = kaishaName;
        this.address = address;
        this.tel = tel;
        this.email = email;
        this.saiyoJisseki = saiyoJisseki;
        this.kinmuChi = kinmuChi;
        this.CompanyChukanList = kaishaChukanList;
    }

    // Getter & Setter
    public String getKaishaId() { return kaishaId; }
    public void setKaishaId(String kaishaId) { this.kaishaId = kaishaId; }

    public String getKaishaName() { return kaishaName; }
    public void setKaishaName(String kaishaName) { this.kaishaName = kaishaName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getSaiyoJisseki() { return saiyoJisseki; }
    public void setSaiyoJisseki(int saiyoJisseki) { this.saiyoJisseki = saiyoJisseki; }

    public String getKinmuChi() { return kinmuChi; }
    public void setKinmuChi(String kinmuChi) { this.kinmuChi = kinmuChi; }

    public List<CompanyChukan> getCompanyChukanList() { return CompanyChukanList; }
    public void setKaishaChukanList(List<CompanyChukan> kaishaChukanList) {
        this.CompanyChukanList = kaishaChukanList;
    }
}