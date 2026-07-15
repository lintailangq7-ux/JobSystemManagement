package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Company;
import model.ModelCompany;   // 企業モデルクラス

public class CompanyDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/jop_managment_system?useSSL=false&serverTimezone=Asia/Tokyo";
    private static final String USER = "root";
    private static final String PASS = "kcsf";
	private String sql;

    /**
     * 企業テーブルから全件を取得
     */
    public List<ModelCompany> findAll() {
        List<ModelCompany> list = new ArrayList<>();
        CompanyChukanDAO CompanyChukanDAO = new CompanyChukanDAO();
        String sql = "SELECT * " +
                     "FROM 企業テーブル " +
                     "ORDER BY 企業ID";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                	ModelCompany c = new ModelCompany();

                    c.setKaishaId(rs.getString("企業ID"));
                    c.setKaishaName(rs.getString("企業名"));
                    c.setAddress(rs.getString("住所"));
                    c.setTel(rs.getString("電話番号"));
                    c.setEmail(rs.getString("メールアドレス"));
                    c.setSaiyoJisseki(rs.getInt("採用実績"));
                    c.setKinmuChi(rs.getString("勤務地"));
                    
                    c.setKaishaChukanList(CompanyChukanDAO.findById(rs.getString("企業ID"))); 
                    
                    list.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("企業データ取得エラー: " + e.getMessage());
        }
        return list;
    }

	public List<Company> findAllCompany() {
		System.out.println("findAllCompany開始");
		// TODO 自動生成されたメソッド・スタブ
		 List<Company> companyList = new ArrayList<>();
		  String sql ="SELECT 企業ID, 企業名, 住所, 電話番号, メールアドレス, 採用実績 FROM `企業テーブル`";;

	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
		    try (Connection con = DriverManager.getConnection(URL,USER,PASS);
		    		 PreparedStatement ps = con.prepareStatement(sql);
		    		ResultSet rs = ps.executeQuery()){


		        while (rs.next()) {
		      

		        	System.out.println("企業ID：" + rs.getString("企業ID"));
		        	System.out.println(rs.getString("企業名"));
		        	System.out.println("データ取得しました");
		            Company company = new Company();

		            company.setId(rs.getString("企業ID"));
		            company.setName(rs.getString("企業名"));
		            company.setAddress(rs.getString("住所"));
		            company.setTel(rs.getString("電話番号"));
		            company.setMail(rs.getString("メールアドレス"));
		            company.setJobtype(rs.getString("採用実績"));

		            companyList.add(company);
		            
		        }
		    
		    }
		    
		    } catch (Exception e) {
		        e.printStackTrace();
		        System.out.println("DAOエラー：" + e.getMessage());
		    }

		    return companyList;
	        }
	
	

	public List<Company> search(String keyword) {
		// TODO 自動生成されたメソッド・スタブ
		 List<Company> companyList = new ArrayList<>();
		 String sql = "SELECT 企業ID, 企業名, 住所, 電話番号, メールアドレス, 採用実績 "
                 + "FROM `企業テーブル` WHERE 企業名 LIKE ?";
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
		    try (Connection con = DriverManager.getConnection(URL,USER,PASS);
		    		 PreparedStatement ps = con.prepareStatement(sql)){
		       

		       
		        ps.setString(1, "%" + keyword + "%");

		        ResultSet rs = ps.executeQuery();

		        while (rs.next()) {

		            Company company = new Company();

		            company.setId(rs.getString("企業ID"));
		            company.setName(rs.getString("企業名"));
		            company.setAddress(rs.getString("住所"));
		            company.setTel(rs.getString("電話番号"));
		            company.setMail(rs.getString("メールアドレス"));
		            company.setJobtype(rs.getString("採用実績"));

		            companyList.add(company);
		        }
		    }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    return companyList;
	        

	
	}}
	        

