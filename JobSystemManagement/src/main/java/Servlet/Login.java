package Servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.LoginDAO;
import DAO.StudentDetailDAO;
import model.ModelLogin;
import model.StudentDetail;
/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		RequestDispatcher dispatcher =
		        request.getRequestDispatcher("/jsp/Login.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		LoginDAO LDao = new LoginDAO();
		ModelLogin Login = new ModelLogin();
		
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        // TODO: 本来はDB照合処理を書く
        // 仮の認証（後で本物に置き換えてください）
        boolean isValid = false;
    	Login = LDao.findId(userId);
    	
    	
    	 StudentDetailDAO dao = new StudentDetailDAO();
    	 StudentDetail detail = dao.findByGakusekiNo(userId.substring(2));
    	 System.out.println(detail.getStudent().getName());
    	 System.out.println(detail);

        if (userId != null && password != null) {
            // 先生ID or 生徒ID の形式チェック（簡易版）
            	if (password.equals(Login.getPassword())) {
            		isValid = true;
            	}
            
     }

        if (isValid) {

            session.setAttribute("userId", userId.substring(2));
            System.out.println(userId);
            session.setAttribute("userType", userId.startsWith("Te") ? "teacher" : "student");
            
            response.sendRedirect("jsp/Employment/EmploymentList.jsp"); // メインメニューへ
        } else {
            request.setAttribute("error", "ユーザーIDまたはパスワードが正しくありません。");
            request.getRequestDispatcher("/jsp/Login.jsp").forward(request, response);
        }
    }
}
