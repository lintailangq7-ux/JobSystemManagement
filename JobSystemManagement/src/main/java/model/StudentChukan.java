package model;

public class StudentChukan {
    private int gakusekiNo;       // 学籍番号
    private String kibouShokushu; // 希望職種

    public StudentChukan() {}

    public StudentChukan(int gakusekiNo, String kibouShokushu) {
        this.gakusekiNo = gakusekiNo;
        this.kibouShokushu = kibouShokushu;
    }

    public int getGakusekiNo() { return gakusekiNo; }
    public void setGakusekiNo(int gakusekiNo) { this.gakusekiNo = gakusekiNo; }

    public String getKibouShokushu() { return kibouShokushu; }
    public void setKibouShokushu(String kibouShokushu) { this.kibouShokushu = kibouShokushu; }
}
