package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.UserDao;

/** ユーザー一覧 */
@WebServlet("/users")
public class UsersServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			req.setAttribute("users", UserDao.findAll(me(req).getId()));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.setAttribute("nav", "users");
		render(req, res, "users.jsp");
	}
}
