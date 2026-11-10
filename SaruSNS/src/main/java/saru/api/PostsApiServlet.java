package saru.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.PostDao;
import saru.model.Post;

@WebServlet("/api/posts")
public class PostsApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			// 今は簡易的に viewerId = 0（未ログイン扱い）で全体のタイムラインを返す
			// 後でトークンからユーザーIDを取るように変更可能
			int viewerId = 0;
			boolean homeOnly = false; // false = 全体, true = 自分+フォロー中のみ

			List<Post> posts = PostDao.timeline(viewerId, homeOnly);

			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < posts.size(); i++) {
				if (i > 0) sb.append(',');
				sb.append(toJson(posts.get(i)));
			}
			sb.append(']');

			res.setContentType("application/json; charset=UTF-8");
			res.setStatus(200);
			res.getWriter().write(sb.toString());
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private static String toJson(Post p) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append("\"id\":").append(p.getId()).append(',');
		sb.append("\"content\":").append(ApiJson.str(p.getText())).append(',');
		sb.append("\"createdAt\":").append(p.getCreatedAt()).append(',');
		sb.append("\"likeCount\":").append(p.getLikeCount()).append(',');
		sb.append("\"liked\":").append(p.isLiked()).append(',');
		sb.append("\"replyCount\":").append(p.getReplyCount()).append(',');
		sb.append("\"user\":{");
		sb.append("\"id\":").append(p.getUserId()).append(',');
		sb.append("\"username\":").append(ApiJson.str(p.getAuthorUser_mail())).append(',');
		sb.append("\"displayName\":").append(ApiJson.str(p.getAuthorUser_name()));
		sb.append('}');
		sb.append('}');
		return sb.toString();
	}
}