package Servlet;

import java.io.IOException;
import java.time.LocalDateTime;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.CompanyDAO;
import DAO.EmploymentChukanDAO;
import DAO.EmploymentDAO;
import model.Company;
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
        CompanyDAO cDAO = new CompanyDAO();
		EmploymentDAO eDAO = new EmploymentDAO();
	    EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();
	    int submitInt;
	    int offerInt;
        
	    String shidoId    = request.getParameter("shidoId");
		String gakusekiNoNum = (String) session.getAttribute("userId");
	    String companyId    = request.getParameter("companyId");
	    String companyName  = request.getParameter("companyName");
	    String place        = request.getParameter("place");
	    String submitStatus = request.getParameter("submitStatus");
	    if (submitStatus.equals("済")) {
	    	submitInt = 0;
	    }else {
	    	submitInt = 1;
	    }
	    
	    String exam         = request.getParameter("exam"); 
	    String examDate = request.getParameter("examDate");
	    LocalDateTime examDateTime = null;
	    if (examDate != null && !examDate.isEmpty()) {
	        examDateTime = LocalDateTime.parse(examDate);
	    }
	    
	    
	    String offerStatus  = request.getParameter("offerStatus");
	    if (offerStatus.equals("内")) {
	    	offerInt = 0;
	    }else {
	    	offerInt = 1;
	    }
	    
	    
	    String acceptDate   = request.getParameter("acceptDate");
	    LocalDateTime acceptDateTime = null;
	    if (examDate != null && !examDate.isEmpty()) {
	    	acceptDateTime = LocalDateTime.parse(acceptDate);
	    }
	    
	    
	    String memo         = request.getParameter("memo");

	    Company C = cDAO.findById(companyName);
	    EmploymentChukan ec = new EmploymentChukan(shidoId,examDateTime, exam, submitInt, place);

		
		eDAO.updateGuidance(gakusekiNoNum, C.getId(), acceptDateTime, offerInt, memo);
		ecDAO.update(ec);
		
		response.sendRedirect(request.getContextPath() + "/ListofEmployment");
	}

}
