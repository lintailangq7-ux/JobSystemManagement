package model;

import java.util.List;

/**
 * 学生1人分の情報 + その学生に紐づく指導一覧（企業情報込み）をまとめて持つクラス。
 * これ1つをJSPに渡せば、突き合わせなしで画面が組める。
 */
public class StudentDetail {
    private ModelStudent student;
    private List<GuidanceDetail> guidanceList;

    public StudentDetail() {}

    public StudentDetail(ModelStudent student, List<GuidanceDetail> guidanceList) {
        this.student = student;
        this.guidanceList = guidanceList;
    }

    public ModelStudent getStudent() { return student; }
    public void setStudent(ModelStudent student) { this.student = student; }

    public List<GuidanceDetail> getGuidanceList() { return guidanceList; }
    public void setGuidanceList(List<GuidanceDetail> guidanceList) { this.guidanceList = guidanceList; }

    /** 内定が1件でもあるか */
    public boolean isNaiteiKakutei() {
        if (guidanceList == null) return false;
        return guidanceList.stream().anyMatch(g -> g.getNaiteiKakutei() == 1);
    }
}
