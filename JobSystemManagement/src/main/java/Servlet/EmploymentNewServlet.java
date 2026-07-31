package Servlet;
 
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.CompanyDAO;
import DAO.EmploymentDAO;
import model.Company;
import model.EmploymentChukan;
import model.ModelStudent;
import model.StudentDetail;
 
@WebServlet("/EmploymentNewServlet")
public class EmploymentNewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
	        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	// 日付の入力フォーマット（例: 2026/06/26）
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
 
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
	 * 試験情報（examContent / examPlace / examDateTime / examSubmit）は
	 * JSP側で複数行分が同じname属性で送信されてくるため、配列として受け取り、
	 * 行数分ループしてまとめて登録する。
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		CompanyDAO cDAO = new CompanyDAO();
		EmploymentDAO eDAO = new EmploymentDAO();
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
		String offerStatus  = request.getParameter("offerStatus");
		String acceptDate    = request.getParameter("acceptDate");
		String memo          = request.getParameter("memo");
 
		// ---- 複数登録される試験情報（配列） ----
		String[] examPlaces   = request.getParameterValues("examPlace");
		String[] examSubmits  = request.getParameterValues("examSubmit");
		String[] examContents = request.getParameterValues("examContent");
		String[] examDates    = request.getParameterValues("examDateTime");
 
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
 
		offerInt = "1".equals(offerStatus) ? 1 : 0;
 
		// ---- 内定日のパース（yyyy/MM/dd 形式） ----
		LocalDateTime acceptDateTime = null;
		if (acceptDate != null && !acceptDate.trim().isEmpty()) {
			try {
				LocalDate d = LocalDate.parse(acceptDate.trim(), DATE_FORMATTER);
				acceptDateTime = d.atStartOfDay();
			} catch (DateTimeParseException e) {
				request.setAttribute("mode", "add");
				request.setAttribute("student", student);
				request.setAttribute("errorMessage", "内定日の形式が正しくありません。（例：2026/06/26）");
				request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
				return;
			}
		}
 
		// ---- 試験情報（複数行）の組み立て ----
		List<EmploymentChukan> examList = new ArrayList<>();
		int rowCount = (examContents != null) ? examContents.length : 0;
 
		for (int i = 0; i < rowCount; i++) {
			String content = getOrEmpty(examContents, i);
			String place   = getOrEmpty(examPlaces, i);
			String submit  = getOrEmpty(examSubmits, i);
			String dateStr = getOrEmpty(examDates, i);
 
			// 企業名・試験会場・試験日時が全て空の行は「空の追加行」とみなしてスキップ
			if (content.isEmpty() && place.isEmpty() && dateStr.isEmpty()) {
				continue;
			}
 
			LocalDateTime examDateTime = null;
			if (!dateStr.isEmpty()) {
				try {
					examDateTime = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
				} catch (DateTimeParseException e) {
					request.setAttribute("mode", "add");
					request.setAttribute("student", student);
					request.setAttribute("errorMessage",
							(i + 1) + "件目の試験日時の形式が正しくありません。（例：2026-08-01 10:00）");
					request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
					return;
				}
			}
 
			int submitInt = "1".equals(submit) ? 1 : 0;
			examList.add(new EmploymentChukan("", examDateTime, content, submitInt, place));
		}
 
		String newId = eDAO.insertGuidanceWithExams(gakusekiNo, C.getId(), acceptDateTime, offerInt, memo, examList);
		if (newId == null) {
			request.setAttribute("mode", "add");
			request.setAttribute("student", student);
			request.setAttribute("errorMessage", "登録に失敗しました。もう一度お試しください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}
 
		response.sendRedirect(request.getContextPath() + "/ListofEmployment");
	}
 
	/**
	 * 配列の指定インデックスの値を安全に取得する。
	 * 配列がnull、範囲外、要素がnullの場合は空文字を返す。
	 */
	private String getOrEmpty(String[] arr, int idx) {
		if (arr == null || idx >= arr.length || arr[idx] == null) {
			return "";
		}
		return arr[idx].trim();
	}
 
	private StudentDetail getStudentDetailFromSession(HttpSession session) {
		Object detailObj = session.getAttribute("detail");
		if (detailObj instanceof StudentDetail) {
			return (StudentDetail) detailObj;
		}
		return null;
	}
 
}