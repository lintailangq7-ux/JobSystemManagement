package saru.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.dao.Db;
import saru.dao.PostDao;
import saru.dao.UserDao;
import saru.model.Post;
import saru.model.User;
/**
 * Servlet implementation class MyPageApiServlet
 */
@WebServlet("/api/mypage/*")
public class MyPageApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       



	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		Integer viewerId = (Integer)request.getAttribute("apiUserId");
		
		String viewerAccountType = "GUEST";
	    if (viewerId != null) {
	        try {
	            User viewerUser = UserDao.findById(viewerId, viewerId);
	            if (viewerUser != null && viewerUser.getType() != null) {
	                viewerAccountType = viewerUser.getType().name();
	            }
	        } catch (Exception e) {
	            viewerAccountType = "GUEST";
	        }
	    }

	    String path = request.getPathInfo() == null ? "" : request.getPathInfo().substring(1);
	    int targetUserId;
		
		try {
			if(path.isBlank()) {
			//パス指定なし → ログイン中の自分の情報		
				if(viewerId == null) {
					ApiJson.error(response, 401, "ログイン情報が必要です");
					return;
				}
				targetUserId = viewerId;
			}else {
				// ID指定 → 他のユーザーの情報
				try {
					targetUserId = Integer.parseInt(path);
				} catch (NumberFormatException e) {
					ApiJson.error(response, 400, "無効なユーザーIDです");
					return;
				}
			}
			
			// ユーザー情報取得（フォロー数・フォロワー数も含む）
			User user = UserDao.findById(targetUserId, viewerId);
			if(user == null) {
				ApiJson.error(response, 404, "ユーザーが見つかりません");
				return;
			}
			
			//  投稿一覧取得
			List<Post> posts = PostDao.byUser(viewerId, targetUserId);

			//  JSONにまとめて返却
			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().write("{"
					+ "\"user\":" + ApiJson.user(user) + ","
					+ "\"posts\":" + ApiJson.posts(posts, viewerAccountType) +
					"}");
			
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
			
		
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		Integer userId = (Integer) req.getAttribute("apiUserId");

		if (userId == null) {
			ApiJson.error(res, 401, "ログインが必要です");
			return;
		}

		String path = req.getPathInfo() == null ? "" : req.getPathInfo().substring(1);
		
		// 自分のプロフィールだけ編集可能
		if (!path.isBlank()) {
			ApiJson.error(res, 403, "自分のプロフィール以外は編集できません");
			return;
		}

		String displayName = trim(req.getParameter("displayName"));
		String bio = trim(req.getParameter("bio"));

		if (displayName.length() > 50) {
			ApiJson.error(res, 400, "アカウント名は50文字までです");
			return;
		}
		if (bio.length() > 200) {
			ApiJson.error(res, 400, "ひとことは200文字までです");
			return;
		}

		try {
			boolean ok = UserDao.updateProfile(userId, displayName, bio);
			if (!ok) {
				ApiJson.error(res, 404, "更新に失敗しました");
				return;
			}
			User updated = UserDao.findById(userId, userId);
			ApiJson.write(res, 200, "{\"ok\":true,\"user\":" + ApiJson.user(updated) + "}");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}
	
	// ✅ 関連データもまとめて削除する場合
	public static boolean delete(int userId) throws SQLException {
	    try (Connection conn = Db.get()) {
	        conn.setAutoCommit(false);
	        try {
	            // フォロー関係を削除
	            try (PreparedStatement ps = conn.prepareStatement(
	                "DELETE FROM follows WHERE follower_id = ? OR followee_id = ?")) {
	                ps.setInt(1, userId);
	                ps.setInt(2, userId);
	                ps.executeUpdate();
	            }
	            // 投稿を削除
	            try (PreparedStatement ps = conn.prepareStatement(
	                "DELETE FROM posts WHERE user_id = ?")) {
	                ps.setInt(1, userId);
	                ps.executeUpdate();
	            }
	            // ユーザー自身を削除
	            int rows;
	            try (PreparedStatement ps = conn.prepareStatement(
	                "DELETE FROM users WHERE id = ?")) {
	                ps.setInt(1, userId);
	                rows = ps.executeUpdate();
	            }
	            conn.commit();
	            return rows > 0;
	        } catch (SQLException e) {
	            conn.rollback();
	            throw e;
	        }
	    }
	}

	

	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
