package Servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.CompanyDAO;
import model.Company;
@WebServlet("/CompanyRegisterServlet")
public class CompanyRegisterServlet extends HttpServlet {

	 @Override
	    protected void doPost(HttpServletRequest request,
	            HttpServletResponse response)
	            throws ServletException, IOException {
		 // 文字化け防止
	        request.setCharacterEncoding("UTF-8");
	        System.out.println("CompanyRegisterServlet 実行");
	        
	       
	        String companyName = request.getParameter("companyName");
	        String address = request.getParameter("address");
	        String tel = request.getParameter("tel");
	        String mail = request.getParameter("mail");
	        String result = request.getParameter("result");

	        
	        System.out.println(companyName);
	        System.out.println(address);
	        
	        
	        Company company = new Company();

	        company.setName(companyName);
	        company.setAddress(address);
	        company.setTel(tel);
	        company.setMail(mail);
	        company.setJobtype(result);

	        CompanyDAO dao = new CompanyDAO();
	        dao.addCompany(company);

	        response.sendRedirect(request.getContextPath() + "/ListofCompanies");
	 }

}
