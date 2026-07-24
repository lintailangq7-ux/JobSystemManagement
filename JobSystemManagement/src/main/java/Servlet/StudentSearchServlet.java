package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.StudentDAO;
import model.ModelStudent;

@WebServlet("/StudentSearchServlet")
public class StudentSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");

        StudentDAO StuDAO = new StudentDAO();
        List<ModelStudent> StuList;

        if (keyword != null && !keyword.isEmpty()) {
            StuList = StuDAO.findByKeyword(keyword);
        } else {
            StuList = StuDAO.findAll();
        }

        request.setAttribute("StuList", StuList);
        request.setAttribute("keyword", keyword);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/StudentList.jsp");
        dispatcher.forward(request, response);
    }
}