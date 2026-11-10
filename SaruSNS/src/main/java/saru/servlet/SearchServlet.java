package saru.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.SearchDAO;
import saru.model.Search;

@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 直接アクセスされた場合は検索画面へ戻す(JSPのパスは実際のものに変更してください)
		response.sendRedirect(request.getContextPath() + "/WEB-INF/jsp/search.jsp");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// 検索方法 1=テキスト 2=画像 3=音声
		String keyword = request.getParameter("SearchText"); // 仮
		int searchMode;
		try {
			searchMode = Integer.parseInt(request.getParameter("SearchMode")); // 仮
		} catch (NumberFormatException e) {
			searchMode = 1; // 不正な値ならテキスト検索扱い
		}

		if (searchMode == 1) { // テキスト
			if (keyword != null && !keyword.isEmpty()) {
				// DAOに検索文字列を渡し、検索結果を取得
				SearchDAO dao = new SearchDAO();
				List<Search> resultList = dao.findByKeyword(keyword);

				// 検索結果を結果画面へ渡す
				request.setAttribute("resultList", resultList);
				request.setAttribute("keyword", keyword);
				request.getRequestDispatcher("/WEB-INF/jsp/searchResult.jsp")
						.forward(request, response);
				return;
			}
			// キーワードが空なら何もせず検索画面へ戻す
//
//			dispatcher.forward(request, response);
//
		} else if (searchMode == 2) { // 画像

		} else if (searchMode == 3) { // 音声

		}
	}
}