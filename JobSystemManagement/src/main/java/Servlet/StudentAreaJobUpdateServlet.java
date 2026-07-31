package Servlet;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.StudentChukanDAO;
import DAO.StudentDAO;
import DAO.StudentDetailDAO;
import model.StudentDetail;

/**
 * 生徒本人が「希望地域」「希望業種1〜3」だけを変更できるServlet。
 *
 * 学生情報の他の項目（クラス・氏名・性別・あっせん状況・在籍状況など）は
 * 一切変更できない、意図的に限定された画面。
 *
 * 対象の学生は、フォームから送られてきた学籍番号を信用するのではなく、
 * セッションに保存されている「ログイン中の本人」の情報（session.getAttribute("detail")）
 * から取得する。これにより、他人の学籍番号を指定して書き換える、といった
 * 不正な操作を防いでいる。
 */
@WebServlet("/StudentAreaJobUpdateServlet")
public class StudentAreaJobUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // =========================================================
    // 変更画面表示
    // =========================================================
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        StudentDetail detail = getStudentDetailFromSession(session);

        if (detail == null || detail.getStudent() == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        request.setAttribute("student", detail.getStudent());

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/jsp/Employment/StudentAreaJobEdit.jsp");
        dispatcher.forward(request, response);
    }

    // =========================================================
    // 変更処理
    // =========================================================
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        StudentDetail detail = getStudentDetailFromSession(session);

        if (detail == null || detail.getStudent() == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        int gakusekiNo = detail.getStudent().getGakusekiNo();

        String area = request.getParameter("area");
        String job1 = request.getParameter("job1");
        String job2 = request.getParameter("job2");
        String job3 = request.getParameter("job3");

        if (area == null || area.trim().isEmpty()) {
            request.setAttribute("student", detail.getStudent());
            request.setAttribute("errorMessage", "希望地域は必須です。");
            request.getRequestDispatcher("/jsp/Employment/StudentAreaJobEdit.jsp")
                   .forward(request, response);
            return;
        }

        StudentDAO studentDAO = new StudentDAO();
        boolean areaOk = studentDAO.updateAreaOnly(gakusekiNo, area.trim());

        StudentChukanDAO chukanDAO = new StudentChukanDAO();
        chukanDAO.updateAll(gakusekiNo, Arrays.asList(job1, job2, job3));

        // セッションの detail を最新の内容に更新しておく
        // （更新後すぐ元の指導一覧画面に戻っても、変更後の内容が反映されるように）
        StudentDetailDAO detailDAO = new StudentDetailDAO();
        StudentDetail refreshed = detailDAO.findByGakusekiNo(String.valueOf(gakusekiNo));
        if (refreshed != null) {
            session.setAttribute("detail", refreshed);
        }

        session.setAttribute("emg", areaOk ? "変更完了" : "変更に失敗しました");

        response.sendRedirect(request.getContextPath() + "/ListofEmployment");
    }

    private StudentDetail getStudentDetailFromSession(HttpSession session) {
        Object detailObj = session.getAttribute("detail");
        if (detailObj instanceof StudentDetail) {
            return (StudentDetail) detailObj;
        }
        return null;
    }
}
