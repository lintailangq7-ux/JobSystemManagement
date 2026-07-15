package model;

public class CompanyChukan {
    private String kaishaId;      // 企業ID
    private String boshuShokushu; // 募集職種

    public CompanyChukan() {}

    public CompanyChukan(String kaishaId, String boshuShokushu) {
        this.kaishaId = kaishaId;
        this.boshuShokushu = boshuShokushu;
    }

    public String getKaishaId() { return kaishaId; }
    public void setKaishaId(String kaishaId) { this.kaishaId = kaishaId; }

    public String getBoshuShokushu() { return boshuShokushu; }
    public void setBoshuShokushu(String boshuShokushu) { this.boshuShokushu = boshuShokushu; }
}