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
import model.ModelStudent;
import model.StudentDetail;

@WebServlet("/EmploymentNewServlet")
public class EmploymentNewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		StudentDetail detail = getStudentDetailFromSession(session);

		request.setAttribute("mode", "add");
		if (detail != null) {
			request.setAttribute("student", detail.getStudent());
		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Employment/Stouroku.jsp");
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

		StudentDetail detail = getStudentDetailFromSession(session);
		if (detail == null || detail.getStudent() == null) {
			// 対象生徒が特定できない場合は処理を進めない
			request.setAttribute("mode", "add");
			request.setAttribute("errorMessage", "対象の生徒情報が取得できませんでした。もう一度ログインし直してください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}
		ModelStudent student = detail.getStudent();
		String gakusekiNo = String.valueOf(student.getGakusekiNo());

		String companyName  = request.getParameter("companyName");
		String place         = request.getParameter("place");
		String submitStatus  = request.getParameter("submitStatus");
		String exam          = request.getParameter("exam");
		String examDate      = request.getParameter("examDate");
		String offerStatus   = request.getParameter("offerStatus");
		String acceptDate    = request.getParameter("acceptDate");
		String memo          = request.getParameter("memo");

		// ---- 入力チェック（未入力のまま送信された場合にNPEで落ちないようにする） ----
		if (companyName == null || companyName.trim().isEmpty()) {
			request.setAttribute("mode", "add");
			request.setAttribute("student", student);
			request.setAttribute("errorMessage", "企業名は必須です。入力してください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}

		Company C = cDAO.findByName(companyName);
		if (C == null) {
			request.setAttribute("mode", "add");
			request.setAttribute("student", student);
			request.setAttribute("errorMessage", "入力された企業名「" + companyName + "」は登録されていません。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}

		// 試験日時はDB上、試験情報の複合主キー(指導ID + 試験日時)の一部であり

		if (examDate == null || examDate.trim().isEmpty()) {
			request.setAttribute("mode", "add");
			request.setAttribute("student", student);
			request.setAttribute("errorMessage", "試験日時は必須です。入力してください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}

		submitInt = "済".equals(submitStatus) ? 1 : 0;
		offerInt  = "内".equals(offerStatus) ? 1 : 0;

		LocalDateTime examDateTime = null;
		if (examDate != null && !examDate.isEmpty()) {
			examDateTime = LocalDateTime.parse(examDate);
		}

		LocalDateTime acceptDateTime = null;
		if (acceptDate != null && !acceptDate.isEmpty()) {
			acceptDateTime = LocalDateTime.parse(acceptDate);
		}
		EmploymentChukan ec = new EmploymentChukan("", examDateTime, exam, submitInt, place);
		String newId = eDAO.insertGuidanceWithExam (gakusekiNo, C.getId(), acceptDateTime, offerInt, memo, ec);
		if (newId == null) {
			request.setAttribute("mode", "add");
			request.setAttribute("student", student);
			request.setAttribute("errorMessage", "登録に失敗しました。もう一度お試しください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}


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