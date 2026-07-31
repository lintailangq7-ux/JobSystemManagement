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


	}
 


	 public Company findById(String companyId) {

       String sql = "SELECT 企業ID, 企業名, 住所, 電話番号, メールアドレス, 採用実績 "

                  + "FROM `企業テーブル` WHERE 企業ID = ?";

       try (Connection con = DriverManager.getConnection(URL, USER, PASS);

            PreparedStatement ps = con.prepareStatement(sql)) {

           ps.setString(1, companyId);

           try (ResultSet rs = ps.executeQuery()) {

               if (rs.next()) {

                   Company company = new Company();

                   company.setId(rs.getString("企業ID"));

                   company.setName(rs.getString("企業名"));

                   company.setAddress(rs.getString("住所"));

                   company.setTel(rs.getString("電話番号"));

                   company.setMail(rs.getString("メールアドレス"));

                   company.setJobtype(rs.getString("採用実績"));

                   return company;

               }

           }

       } catch (Exception e) {

           e.printStackTrace();

           System.out.println("企業取得エラー: " + e.getMessage());

       }

       return null;

   }
 
  public Company findByName(String companyName) {

           	System.out.println("企業名検索OK1");

       String sql = "SELECT 企業ID, 企業名, 住所, 電話番号, メールアドレス, 採用実績 "

                  + "FROM `企業テーブル` WHERE 企業名 = ?";

       try (Connection con = DriverManager.getConnection(URL, USER, PASS);

            PreparedStatement ps = con.prepareStatement(sql)) {

                System.out.println("企業名検索OK2");

           ps.setString(1, companyName);

           try (ResultSet rs = ps.executeQuery()) {

               if (rs.next()) {

                   Company company = new Company();

                   company.setId(rs.getString("企業ID"));

                   company.setName(rs.getString("企業名"));

                   company.setAddress(rs.getString("住所"));

                   company.setTel(rs.getString("電話番号"));

                   company.setMail(rs.getString("メールアドレス"));

                   company.setJobtype(rs.getString("採用実績"));

                   System.out.println("企業名検索OK3");

                   return company;

               }

           }

       } catch (Exception e) {

           e.printStackTrace();

           System.out.println("企業取得エラー: " + e.getMessage());

       }

       return null;

   }
 



public void addCompany(Company company) {

    String sql =

        "INSERT INTO 企業テーブル "

      + "(企業ID,企業名, 住所, 電話番号, メールアドレス, 採用実績) "

      + "VALUES (?,?, ?, ?, ?, ?)";

    try {

        Connection conn =

                DriverManager.getConnection(URL, USER, PASS);


        // 企業ID作成

        String companyId = createCompanyId(conn);

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, companyId);

        ps.setString(2, company.getName());

        ps.setString(3, company.getAddress());

        ps.setString(4, company.getTel());

        ps.setString(5, company.getMail());

        ps.setInt(6, Integer.parseInt(company.getJobtype()));

        ps.executeUpdate();

        ps.close();

        conn.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

}
 
  public String generateNewKaishaId() {

      String sql = "SELECT 企業ID FROM 企業テーブル ORDER BY 企業ID DESC LIMIT 1";

      String newId = "C0001"; // データが1件もない場合の初期値

	       try (Connection con = DriverManager.getConnection(URL, USER, PASS);

           PreparedStatement ps = con.prepareStatement(sql);

           ResultSet rs = ps.executeQuery()) {

	           if (rs.next()) {

              String lastId = rs.getString("企業ID"); // 例: "C0007"

              String numPart = lastId.substring(1);   // "C" を除いた "0007"

              int num = Integer.parseInt(numPart);     // 7

              num++;                                   // 8

	               newId = String.format("C%04d", num);     // "C0008"

          }

	       } catch (Exception e) {

          e.printStackTrace();

          System.out.println("企業ID採番エラー: " + e.getMessage());

      }

	       return newId;

  }
 
private String createCompanyId(Connection conn) throws Exception {

    String sql =

        "SELECT MAX(企業ID) FROM 企業テーブル";

    PreparedStatement ps =

        conn.prepareStatement(sql);

    ResultSet rs = ps.executeQuery();

    if(rs.next() && rs.getString(1) != null) {

        String lastId = rs.getString(1);

        int number =

            Integer.parseInt(lastId.substring(1));

        return String.format("C%04d", number + 1);

    } else {

        return "C0001";

    }

}

public void updateCompany(Company company) {

	// TODO 自動生成されたメソッド・スタブ

	 String sql =

       "UPDATE 企業テーブル SET "

     + "企業名=?, 住所=?, 電話番号=?, メールアドレス=?, 採用実績=? "

     + "WHERE 企業ID=?";

		    try {

		        Connection conn =

           DriverManager.getConnection(URL, USER, PASS);

		        PreparedStatement ps =

           conn.prepareStatement(sql);

		        ps.setString(1, company.getName());

       ps.setString(2, company.getAddress());

       ps.setString(3, company.getTel());

       ps.setString(4, company.getMail());

       ps.setInt(5, Integer.parseInt(company.getJobtype()));

       ps.setString(6, company.getId());


       int count = ps.executeUpdate();

       System.out.println("更新件数：" + count);

		    } catch(Exception e) {

       e.printStackTrace();

   }

}


public void deleteCompany(String companyId) {

    String sql = "DELETE FROM 企業テーブル WHERE 企業ID = ?";

    try {

        Connection conn =

                DriverManager.getConnection(URL, USER, PASS);

        PreparedStatement ps =

                conn.prepareStatement(sql);

        ps.setString(1, companyId);

        ps.executeUpdate();

        ps.close();

        conn.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

}

}
 
 