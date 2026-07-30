package Servlet;
import java.io.IOException;
import java.util.List;

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
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Login.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("Login");

        LoginDAO loginDao = new LoginDAO();
        StudentDetailDAO detailDao = new StudentDetailDAO();

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        HttpSession session = request.getSession();

        boolean isValid = false;
        String redirectPath = null;

        if (userId != null && password != null) {
            System.out.println("userId !="+userId+" && password !="+password);

            // ユーザー情報をDBから取得
            ModelLogin login = loginDao.findId(userId);

            if (login != null) {
                if (userId.startsWith("Te")) {
                    // 先生ログイン：パスワード照合
                    if (password.equals(login.getPassword())) {
                        isValid = true;
                        List<StudentDetail> detailList = detailDao.findAllStudentDetail();
                        session.setAttribute("detail", detailList);
                        redirectPath = "jsp/Employment/TecherEmplymentList.jsp";
                    }
                } else if (userId.startsWith("Su")) {
                    // 生徒ログイン：パスワード照合を追加
                    System.out.println("Su");
                    if (password.equals(login.getPassword())) {
                        isValid = true;
                        StudentDetail detail = detailDao.findByGakusekiNo(userId.substring(2));
                        session.setAttribute("userId", userId);
                        session.setAttribute("detail", detail);
                        redirectPath = "jsp/Employment/EmploymentList.jsp";
                    }
                }
            } else {
                System.out.println("該当するユーザーが見つかりません: " + userId);
            }
        }

        if (isValid) {
            session.setAttribute("userId", userId);
            session.setAttribute("userType", userId.startsWith("Te") ? "teacher" : "student");
            response.sendRedirect(redirectPath); // メインメニューへ
        } else {
            request.setAttribute("error", "ユーザーIDまたはパスワードが正しくありません。");
            request.getRequestDispatcher("/jsp/Login.jsp").forward(request, response);
        }
    }
}