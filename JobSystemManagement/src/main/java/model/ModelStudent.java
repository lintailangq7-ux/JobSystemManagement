package model;

//3. Gakusei.java（学生テーブル）
import java.util.List;

public class ModelStudent {

    // 学生メイン情報
    private int gakusekiNo;           // 学籍番号
    private String className;         // クラス
    private String name;              // 氏名
    private int attendanceNo;         // 出席番号
    private int zaisekiJokyo;         // 在籍状況
    private String kenNaiGaiKibo;     // 県内外の希望
    private String seibetsu;          // 性別
    private String biko;              // 備考

    // 学生中間テーブル（希望職種：複数）
    private List<StudentChukan> StudentChukanList;

    public ModelStudent() {}

    public ModelStudent(int gakusekiNo, String className, String name, int attendanceNo,
                        int zaisekiJokyo, String kenNaiGaiKibo, String seibetsu, String biko,
                        List<StudentChukan> gakuseiChukanList) {
        this.gakusekiNo = gakusekiNo;
        this.className = className;
        this.name = name;
        this.attendanceNo = attendanceNo;
        this.zaisekiJokyo = zaisekiJokyo;
        this.kenNaiGaiKibo = kenNaiGaiKibo;
        this.seibetsu = seibetsu;
        this.biko = biko;
        this.StudentChukanList = gakuseiChukanList;
    }

    // Getter & Setter
    //学籍
    public int getGakusekiNo() { return gakusekiNo; }
    public void setGakusekiNo(int gakusekiNo) { this.gakusekiNo = gakusekiNo; }

    //クラス
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    //氏名
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    //あっせん状況
    public int getAttendanceNo() { return attendanceNo; }
    public void setAttendanceNo(int attendanceNo) { this.attendanceNo = attendanceNo; }

    //在籍状況
    public int getZaisekiJokyo() { return zaisekiJokyo; }
    public void setZaisekiJokyo(int zaisekiJokyo) { this.zaisekiJokyo = zaisekiJokyo; }

    //県内外
    public String getKenNaiGaiKibo() { return kenNaiGaiKibo; }
    public void setKenNaiGaiKibo(String kenNaiGaiKibo) { this.kenNaiGaiKibo = kenNaiGaiKibo; }

    //性別
    public String getSeibetsu() { return seibetsu; }
    public void setSeibetsu(String seibetsu) { this.seibetsu = seibetsu; }

    //備考
    public String getBiko() { return biko; }
    public void setBiko(String biko) { this.biko = biko; }

    public List<StudentChukan> getStudentChukanList() { return StudentChukanList; }
    public void setStudentChukanList(List<StudentChukan> gakuseiChukanList) {
        this.StudentChukanList = gakuseiChukanList;
    }
}