package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.UserDao;
import sanzaru.model.User;

/** フォロー / フォロー解除 */
@WebServlet("/follow")
public class FollowServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		User me = me(req);
		try {
			User target = UserDao.findByUsername(me.getId(), String.valueOf(req.getParameter("name")));
			if (target == null) {
				flashError(req, "ユーザーが見つかりません");
			} else if (target.getId() == me.getId()) {
				flashError(req, "自分はフォローできません");
			} else {
				UserDao.toggleFollow(me.getId(), target.getId());
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		redirectBack(req, res, null);
	}
}
