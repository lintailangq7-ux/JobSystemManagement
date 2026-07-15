package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.CompanyDAO;
import model.Company;

@WebServlet("/ListofCompanies")
public class ListofCompanies extends HttpServlet {
	
	  @Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {

	        // JSPから検索文字を受け取る
		 

	       String keyword = request.getParameter("keyword");
	        

	        //DAOで検索
	        CompanyDAO dao = new CompanyDAO();
	        List<Company> companyList;
	        
	        if (keyword == null || keyword.trim().isEmpty()) {
	           companyList = dao.findAllCompany();      // 初回表示：全件取得
	        } else {
	            companyList = dao.search(keyword); // 検索時：条件に合うものだけ取得
	        }
	        System.out.println("keyword = " + keyword);
	        System.out.println("件数 = " + companyList.size());

	        // JSPへ渡す
	       
	        request.setAttribute("companyList", companyList);

	        // 一覧画面へ戻る
	       RequestDispatcher rd =request.getRequestDispatcher("/jsp/ListofCompanies.jsp");

	        rd.forward(request, response);

}
}