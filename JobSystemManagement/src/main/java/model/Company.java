package model;

import java.util.List;

public class Company {
	    private String id;//ID
	    private String name;//企業名
	    private String address;//住所
	    private String tel;//電話番号
	    private String mail;//メールアドレス
	    private String jobtype;//募集職種

private List<Company>CompanyList;
public Company() {}
public Company(String id,String name,String address,String tel,String mail,String jobtype,List<Company> CompanyList) {
	this.id=id;
	this.name=name;
	this.address=address;
	this.tel=tel;
	this.mail=mail;
	this.jobtype=jobtype;
	this.CompanyList=CompanyList;
}
	    
		public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }


	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }


	    public String getAddress() {
	        return address;
	    }

	    public void setAddress(String address) {
	        this.address = address;
	    }


	    public String getTel() {
	        return tel;
	    }

	    public void setTel(String tel) {
	        this.tel = tel;
	    }


	    public String getMail() {
	        return mail;
	    }

	    public void setMail(String mail) {
	        this.mail = mail;
	    }


	    public String getJobtype() {
	        return jobtype;
	    }

	    public void setJobtype(String jobtype) {
	        this.jobtype = jobtype;
	    }
	    public List<Company> getCompanyList(){
	    	return CompanyList;
	    	
	    }
	    public void setCompanyList(List<Company> companyList) {
	    	this.CompanyList=companyList;
	    }
}
