package Servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.EmploymentChukanDAO;
import DAO.EmploymentDAO;
import model.EmploymentChukan;

/**
 * Servlet implementation class EmploymentCangeServlet
 */
@WebServlet("/EmploymentCangeServlet")
public class EmploymentCangeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
		String gakusekiNoNum = (String) session.getAttribute("userId");
	    String companyId    = request.getParameter("companyId");
	    String companyName  = request.getParameter("companyName");
	    String place        = request.getParameter("place");
	    String submitStatus = request.getParameter("submitStatus");
	    String exam         = request.getParameter("exam");
	    String examDate     = request.getParameter("examDate");
	    String offerStatus  = request.getParameter("offerStatus");
	    String acceptDate   = request.getParameter("acceptDate");
	    String memo         = request.getParameter("memo");
		
	    EmploymentChukanDAO dao = new EmploymentChukanDAO();

	    EmploymentChukan ec = new EmploymentChukan(offerStatus, null, acceptDate, 0, memo);
		EmploymentDAO eDAO = new EmploymentDAO();
		
		eDAO.updateGuidance(gakusekiNoNum, gakusekiNoNum, null, 0, gakusekiNoNum);
	}

}
