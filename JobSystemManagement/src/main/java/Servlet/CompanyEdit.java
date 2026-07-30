package Servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.CompanyDAO;
import model.Company;
@WebServlet("/CompanyEdit")
public class CompanyEdit extends HttpServlet {
	 protected void doGet(HttpServletRequest request,
	            HttpServletResponse response)
	            throws ServletException, IOException {

	        String companyId = request.getParameter("companyId");

	        CompanyDAO dao = new CompanyDAO();

	        Company company = dao.findById(companyId);

	        request.setAttribute("company", company);

	        request.getRequestDispatcher("/jsp/Khenkou.jsp")
	               .forward(request, response);
	    }

}
