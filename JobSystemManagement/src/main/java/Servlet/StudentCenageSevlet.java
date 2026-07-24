package Servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.StudentDAO;
import model.ModelStudent;

@WebServlet("/StudentCenageSevlet")
public class StudentCenageSevlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gakusekiNoStr = request.getParameter("gakusekiNo");
        String forword = "/jsp/StudentList.jsp";

        if (gakusekiNoStr != null && !gakusekiNoStr.isEmpty()) {
            try {
                int gakusekiNo = Integer.parseInt(gakusekiNoStr);
                StudentDAO dao = new StudentDAO();
                ModelStudent student = dao.findByGakusekiNo(gakusekiNo);

                if (student != null) {
                    request.setAttribute("student", student);
                    forword = "/jsp/GHenkou.jsp";   // ← ここを変更
                } else {
                    request.setAttribute("emg", "対象の学生が見つかりませんでした");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("emg", "学籍番号の形式が不正です");
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(forword);
        dispatcher.forward(request, response);
    }
}