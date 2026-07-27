package Servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.StudentChukanDAO;
import DAO.StudentDAO;

@WebServlet("/StudentDeleteServlet")
public class StudentDeleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String gakusekiNoStr = request.getParameter("gakusekiNo");
        String message;

        StudentDAO StuDAO = new StudentDAO();

        if (gakusekiNoStr != null && !gakusekiNoStr.isEmpty()) {
            try {
                int gakusekiNo = Integer.parseInt(gakusekiNoStr);

                StudentChukanDAO chukanDao = new StudentChukanDAO();
                chukanDao.deleteByGakusekiNo(gakusekiNo);

                boolean success = StuDAO.delete(gakusekiNo);
                message = success ? "削除完了" : "削除に失敗しました";

            } catch (NumberFormatException e) {
                message = "学籍番号の形式が不正です";
            }
        } else {
            message = "学籍番号が指定されていません";
        }

        // メッセージだけ一時的にセッションに入れる（一覧表示側で読んだら即削除する）
        HttpSession session = request.getSession();
        session.setAttribute("emg", message);

        // POST処理後はリダイレクト（GET）で一覧を表示し直す
        response.sendRedirect(request.getContextPath() + "/StudentListServlet");
    }
}