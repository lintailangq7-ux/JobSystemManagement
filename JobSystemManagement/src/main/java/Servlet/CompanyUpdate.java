package Servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.CompanyDAO;
import model.Company;
@WebServlet("/CompanyUpdate")
public class CompanyUpdate extends HttpServlet {

	 protected void doPost(
	            HttpServletRequest request,
	            HttpServletResponse response)
	            throws ServletException, IOException {

	        request.setCharacterEncoding("UTF-8");

	        String id = request.getParameter("companyId");
	        String name = request.getParameter("companyName");
	        String address = request.getParameter("address");
	        String tel = request.getParameter("tel");
	        String mail = request.getParameter("mail");
	        String result = request.getParameter("result");

	        Company company = new Company();

	        company.setId(id);
	        company.setName(name);
	        company.setAddress(address);
	        company.setTel(tel);
	        company.setMail(mail);
	        company.setJobtype(result);

	        CompanyDAO dao = new CompanyDAO();

	        dao.updateCompany(company);

	        response.sendRedirect(
	            request.getContextPath()
	            + "/ListofCompanies"
	        );
	        
	    }
}
