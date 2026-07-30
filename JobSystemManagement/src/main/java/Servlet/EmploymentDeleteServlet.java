package Servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.EmploymentChukanDAO;
import DAO.EmploymentDAO;

/**
 * Servlet implementation class EmploymentDeleteServlet
 */
@WebServlet("/EmploymentDeleteServlet")
public class EmploymentDeleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
	    String shidoId    = request.getParameter("shidoId");

		
	    EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();
		EmploymentDAO eDAO = new EmploymentDAO();
		
		System.out.println("delete");
		eDAO.deleteGuidance(shidoId);

		
		response.sendRedirect(request.getContextPath() + "/ListofEmployment");
	}

}
