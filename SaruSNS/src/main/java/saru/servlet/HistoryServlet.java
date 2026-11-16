package saru.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import saru.dao.HistoryDAO;
import saru.model.History;

/**
 * Servlet implementation class HistoryServlet
 */
@WebServlet("/HistoryServlet")
public class HistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		
		
		
		//何かしらとる
//		LookHistory
//		GoodHistory
//		BookMark
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HistoryDAO DAO = new HistoryDAO();
		List<History> list = new ArrayList<>();
		int user_id = Integer.parseInt("user_id");
		int HistoryMode = Integer.parseInt("HistoryMode");
        HttpSession session = request.getSession(); //仮
        
		if(user_id != 0 && HistoryMode !=0) {
			list = DAO.findHistory(user_id, HistoryMode);
			session.setAttribute("History", list);
		}else {
			System.out.println("エラー");
		}
		
		
		
	}

}
