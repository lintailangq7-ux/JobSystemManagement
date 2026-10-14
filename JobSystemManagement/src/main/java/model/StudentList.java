package model;

public class StudentList {

    // フィールド
    private String classNo;       // クラス番号
    private String attendanceNo;  // 出席番号
    private String studentName;   // 氏名
    private String kana;          // 読み仮名

    // コンストラクタ
    public StudentList() {

    }

    // クラス番号
    public String getClassNo() {
        return classNo;
    }

    public void setClassNo(String classNo) {
        this.classNo = classNo;
    }

    // 出席番号
    public String getAttendanceNo() {
        return attendanceNo;
    }

    public void setAttendanceNo(String attendanceNo) {
        this.attendanceNo = attendanceNo;
    }

    // 氏名
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    // 読み仮名
    public String getKana() {
        return kana;
    }

    public void setKana(String kana) {
        this.kana = kana;
    }
}