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

import DAO.StudentDAO;
import model.ModelStudent;

@WebServlet("/StudentListServlet")
public class StudentListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StudentDAO StuDAO = new StudentDAO();
        List<ModelStudent> StuList = StuDAO.findAll();
        request.setAttribute("StuList", StuList);

        // セッションに一時保存されたメッセージがあれば取り出して、その場で消す
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object emg = session.getAttribute("emg");
            if (emg != null) {
                request.setAttribute("emg", emg);
                session.removeAttribute("emg"); // 一度表示したら消す（再読み込みで出ないように）
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/StudentList.jsp");
        dispatcher.forward(request, response);
    }
}