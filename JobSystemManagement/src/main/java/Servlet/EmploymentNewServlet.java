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
 
	/**
	 * 追加画面表示。
	 * 対象の生徒は、Login.java / ReportServlet等でセッションにセットされている
	 * "detail"（StudentDetail、今まさに一覧を表示している生徒）から取得する。
	 * request.getParameter("studentNo") で取ろうとしても、
	 * EmploymentList.jspの「追加」ボタンはパラメータを渡していないため取得できない。
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		StudentDetail detail = getStudentDetailFromSession(session);
 
		request.setAttribute("mode", "add");
		if (detail != null) {
			request.setAttribute("student", detail.getStudent());
		}
 
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
 
		String newId = eDAO.insertGuidance(gakusekiNo, C.getId(), acceptDateTime, offerInt, memo);
		if (newId == null) {
			request.setAttribute("mode", "add");
			request.setAttribute("student", student);
			request.setAttribute("errorMessage", "登録に失敗しました。もう一度お試しください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}
 
		EmploymentChukan ec = new EmploymentChukan(newId, examDateTime, exam, submitInt, place);
		ecDAO.insert(ec);
 
		response.sendRedirect(request.getContextPath() + "/ListofEmployment");
	}
 
	/**
	 * セッションの "detail" 属性から StudentDetail を安全に取り出す。
	 * 教師ログイン直後（生徒一覧選択前）は List&lt;StudentDetail&gt; が
	 * 入っている場合があるため、その場合は null を返す。
	 */
	private StudentDetail getStudentDetailFromSession(HttpSession session) {
		Object detailObj = session.getAttribute("detail");
		if (detailObj instanceof StudentDetail) {
			return (StudentDetail) detailObj;
		}
		return null;
	}
 
}