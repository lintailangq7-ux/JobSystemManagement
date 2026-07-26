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

@WebServlet("/EmploymentNewServlet")
public class EmploymentNewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
	    	submitInt = 1;
	    }else {
	    	submitInt = 0;
	    }
	    
	    String exam         = request.getParameter("exam"); 
	    String examDate = request.getParameter("examDate");
	    LocalDateTime examDateTime = null;
	    if (examDate != null && !examDate.isEmpty()) {
	        examDateTime = LocalDateTime.parse(examDate);
	    }
	    
	    
	    String offerStatus  = request.getParameter("offerStatus");
	    if (offerStatus.equals("内")) {
	    	offerInt = 1;
	    }else {
	    	offerInt = 0;
	    }
	    
	    
	    String acceptDate   = request.getParameter("acceptDate");
	    LocalDateTime acceptDateTime = null;
	    if (examDate != null && !examDate.isEmpty()) {
	    	acceptDateTime = LocalDateTime.parse(acceptDate);
	    }
	    
	    
	    String memo         = request.getParameter("memo");

	    Company C = cDAO.findByName(companyName);


		
		String newId = eDAO.insertGuidance(gakusekiNoNum.substring(2), C.getId(), acceptDateTime, offerInt, memo);
	    EmploymentChukan ec = new EmploymentChukan(newId,examDateTime, exam, submitInt, place);
		ecDAO.insert(ec);
		
    	session.setAttribute("userId",  gakusekiNoNum);
		response.sendRedirect(request.getContextPath() + "/ListofEmployment");
	}

}


//// 指導一覧（メイン情報）
//private String shidoId;           // 指導ID
//private int gakusekiNo;           // 学籍番号
//private String kaishaId;          // 企業ID
//private LocalDateTime naiteiKakuteiBi;   // 内定確定日
//private int naiteiKakutei;        // 内定確定
//private String biko;              // 備考
//
//// 就職情報中間テーブル（複数）