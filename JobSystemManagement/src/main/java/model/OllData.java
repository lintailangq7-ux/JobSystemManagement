package model;

import java.util.List;

/**
 * 企業情報・指導情報・学生情報をまとめて保持するデータクラス。
 *
 * JSPのEL式（${ollData.company.kaishaName} など）で参照できるよう、
 * フィールド名と対応する正しいgetter/setter名に修正しています。
 * （元コードは getKaishaId() が ModelCompany を返す等、名前と中身が
 *   一致しておらず、JavaBean規約上EL式から正しく参照できませんでした）
 */
public class OllData {

    private List<ModelCompany> company;       // 企業情報
    private List<ModelEmployment> employment; // 指導情報（就職情報）
    private List<ModelStudent> student;       // 学生情報

    public OllData() {}

    public OllData(List<ModelCompany> company, List<ModelEmployment> employment, List<ModelStudent> student) {
        this.company = company;
        this.employment = employment;
        this.student = student;
    }

    public List<ModelCompany> getCompany() { return company; }
    public void setCompany(List<ModelCompany> company) { this.company = company; }

    public List<ModelEmployment> getEmployment() { return employment; }
    public void setEmployment(List<ModelEmployment> employment) { this.employment = employment; }

    public List<ModelStudent> getStudent() { return student; }
    public void setStudent(List<ModelStudent> student) { this.student = student; }
}



