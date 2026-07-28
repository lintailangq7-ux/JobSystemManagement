package Servlet;
 
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
import model.ModelEmployment;
import model.StudentDetail;
 
/**
 * Servlet implementation class EmploymentCangeServlet
 */
@WebServlet("/EmploymentCangeServlet")
public class EmploymentCangeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	/**
	 * 変更画面表示。
	 * 一覧からPOSTされてきた shidoId を使って既存データをDBから取得し、
	 * Shenkou.jsp に「編集モード」であることと既存データを渡す。
	 * ここが抜けていたため、これまでは常に空欄の追加フォームが表示されていた。
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String shidoId = request.getParameter("shidoId");
		
		HttpSession session = request.getSession();
 
		String userId = (String) session.getAttribute("userId"); 
		System.out.println(userId + "EmploymentCangeServlet");
		EmploymentDAO eDAO = new EmploymentDAO();
		EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();
		CompanyDAO cDAO = new CompanyDAO();
 
		ModelEmployment employment = eDAO.findById(shidoId); // ※EmploymentDAOに追加が必要（下記参照）
		if (employment == null) {
			// 対象データが無い場合は一覧へ戻す
			response.sendRedirect(request.getContextPath() + "/ListofEmployment");
			return;
		}
 
		Company company = cDAO.findById(employment.getKaishaId());
		List<EmploymentChukan> chukanList = ecDAO.findById(shidoId);
		EmploymentChukan latestChukan = (chukanList != null && !chukanList.isEmpty())
				? chukanList.get(0)
				: new EmploymentChukan();
 
		request.setAttribute("mode", "edit");
		request.setAttribute("shidoId", shidoId);
		request.setAttribute("employment", employment);
		request.setAttribute("company", company);
		request.setAttribute("chukan", latestChukan);
 
		// 表示用：左側の生徒情報ボックスに使う（今表示中の生徒＝セッションのdetail）

		Object detailObj = session.getAttribute("detail");
		if (detailObj instanceof StudentDetail) {
			request.setAttribute("student", ((StudentDetail) detailObj).getStudent());
		}
 
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp");
		dispatcher.forward(request, response);
	}
 
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CompanyDAO cDAO = new CompanyDAO();
		EmploymentDAO eDAO = new EmploymentDAO();
		EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();
		int submitInt;
		int offerInt;
 
		// 更新対象のキーは必ずフォームのhidden項目から受け取った shidoId を使う。
		// （修正前はここでログインユーザーのIDを使っており、更新先の行を取り違えていた）
		String shidoId = request.getParameter("shidoId");
		String companyName  = request.getParameter("companyName");
		String place         = request.getParameter("place");
		String submitStatus  = request.getParameter("submitStatus");
		String exam           = request.getParameter("exam");
		String examDate       = request.getParameter("examDate");
		String offerStatus    = request.getParameter("offerStatus");
		String acceptDate     = request.getParameter("acceptDate");
		String memo            = request.getParameter("memo");
 
		// ---- 入力チェック ----
		if (shidoId == null || shidoId.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/ListofEmployment");
			return;
		}
		if (companyName == null || companyName.trim().isEmpty()) {
			request.setAttribute("mode", "edit");
			request.setAttribute("shidoId", shidoId);
			request.setAttribute("errorMessage", "企業名は必須です。入力してください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}
 
		Company C = cDAO.findByName(companyName);
		if (C == null) {
			request.setAttribute("mode", "edit");
			request.setAttribute("shidoId", shidoId);
			request.setAttribute("errorMessage", "入力された企業名「" + companyName + "」は登録されていません。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}
 
		// 試験日時は複合主キー(指導ID + 試験日時)の一部でNULL不可。
		// 未入力のままUPDATEすると制約違反になるため必須チェックする。
		if (examDate == null || examDate.trim().isEmpty()) {
			request.setAttribute("mode", "edit");
			request.setAttribute("shidoId", shidoId);
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
 
		EmploymentChukan ec = new EmploymentChukan(shidoId, examDateTime, exam, submitInt, place);
 
		eDAO.updateGuidance(shidoId, C.getId(), acceptDateTime, offerInt, memo, ec);
		boolean chukanOk = ecDAO.update(ec);
		if (!chukanOk) {
			request.setAttribute("mode", "edit");
			request.setAttribute("shidoId", shidoId);
			request.setAttribute("errorMessage", "試験情報の更新に失敗しました。入力内容を確認してください。");
			request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp").forward(request, response);
			return;
		}
 
		response.sendRedirect(request.getContextPath() + "/ListofEmployment");
	}
 
}