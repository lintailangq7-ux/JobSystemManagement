package sanzaru.servlet;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sanzaru.dao.PostDao;

/** ホーム (自分とフォロー中の人の投稿) と、みんなの投稿 */
@WebServlet({ "", "/explore" })
public class TimelineServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		boolean home = !"/explore".equals(req.getServletPath());
		try {
			req.setAttribute("posts", PostDao.timeline(me(req).getId(), home));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		req.setAttribute("nav", home ? "home" : "explore");
		req.setAttribute("title", home ? "ホーム" : "みんなの投稿");
		req.setAttribute("emptyText", home
				? "まだ投稿がありません。最初の投稿をしたり、「ユーザー」から誰かをフォローしてみましょう。"
				: "まだ誰も投稿していません。");
		render(req, res, "timeline.jsp");
	}
}
