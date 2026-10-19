package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.PostDao;
import sanzaru.dao.UserDao;
import sanzaru.model.User;

/** プロフィール画面 (GET /user?name=xxx) と、自分のプロフィールの更新 (POST /user) */
@WebServlet("/user")
public class ProfileServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		User me = me(req);
		String name = req.getParameter("name");
		try {
			User user = name == null || name.isBlank() ? me : UserDao.findByUsername(me.getId(), name);
			if (user == null) {
				notFound(req, res, "ユーザーが見つかりません");
				return;
			}
			req.setAttribute("user", user);
			req.setAttribute("posts", PostDao.byUser(me.getId(), user.getId()));
			req.setAttribute("editing", user.getId() == me.getId() && req.getParameter("edit") != null);
			if (user.getId() == me.getId()) req.setAttribute("nav", "me");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		render(req, res, "profile.jsp");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		User me = me(req);
		String displayName = LoginServlet.trim(req.getParameter("displayName"));
		String bio = req.getParameter("bio");
		try {
			UserDao.updateProfile(me.getId(),
					displayName.isEmpty() ? me.getDisplayName() : LoginServlet.cut(displayName, 50),
					bio == null ? me.getBio() : LoginServlet.cut(bio, 160));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		flash(req, "保存しました");
		redirect(req, res, "/user?name=" + me.getUsername());
	}
}
