package Servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/EmploymentNewServlet")
public class EmploymentNewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 String gakusekiNoNum = request.getParameter("studentNo");
	}

}


//// 指導一覧（メイン情報）
//private String shidoId;           // 指導ID
//private int gakusekiNo;           // 学籍番号
//private String kaishaId;          // 企業ID
//private LocalDateTime naiteiKakuteiBi;   // 内定確定日
//private int naiteiKakutei;        // 内定確定
//private String biko;              // 備考
//
//// 就職情報中間テーブル（複数）