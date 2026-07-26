package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.StudentDetailDAO;
import model.StudentDetail;

/**
 * Servlet implementation class ListofEmployment
 */
@WebServlet("/ListofEmployment")
public class ListofEmployment extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ListofEmployment() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        // TODO: 本来はDB照合処理を書く
        // 仮の認証（後で本物に置き換えてください）
        

    	
    	StudentDetailDAO dao = new StudentDetailDAO();
    	String responsed = null;



        	System.out.println("userId != null && password != null");
            // 先生ID or 生徒ID の形式チェック（簡易版）
        	if(userId.startsWith("Te")) {
        		List<StudentDetail> detail;
        		  if (keyword == null || keyword.trim().isEmpty()) {
        			  	detail = dao.findAllStudentDetail();
        	        } else {
        	        	detail = dao.findByCompanyKeyword(keyword); // 検索時：条件に合うものだけ取得
        	        }
            		
            		session.setAttribute("detail", detail);
            		responsed = "jsp/Employment/TecherEmplymentList.jsp";
            }else if(userId.startsWith("St")) {
            		StudentDetail detail = dao.findByGakusekiNo(userId.substring(2));
            		session.setAttribute("userId", userId);
                    session.setAttribute("detail", detail);
            		responsed = "jsp/Employment/EmploymentList.jsp";
            }
            



        	session.setAttribute("userId", userId);
            session.setAttribute("userType", userId.startsWith("Te") ? "teacher" : "student");
            
            response.sendRedirect(responsed); // メインメニューへ

    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
	
    }

}
