package saru.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.AccountType;
import saru.dao.ComentDAO;
/**
 * Servlet implementation class ComentServlet
 */
@WebServlet("/ComentServlet")
public class ComentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		if(id != 0) {
			//ユーザーを探す？
			ComentDAO DAO = new ComentDAO();
//			Coment Coment = DAO.findById(id);
		}else {
			System.out.println("エラー");
		}
	}

	//コメント投稿
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int id              = Integer.parseInt(request.getParameter("id"));
		String user_mail    = request.getParameter("user_mail");
		String user_name    = request.getParameter("user_name");
		String coment       = request.getParameter("coment");
		String bio          = request.getParameter("bio");
		AccountType type    = AccountType.valueOf(request.getParameter("type"));
		long createdAt      = Long.parseLong(request.getParameter("createdAt"));
		
		boolean isEdit =(id !=0);
		
//		Coment Add = new Coment();
//		Add.setId(id);
//		Add.setUser_mail(user_mail);
//		Add.setUser_name(user_name);
//		Add.setComent(coment);
//		Add.setBio(bio);
//		Add.setType(type);
//		Add.setCreatedAt(createdAt);
	}

	

}
