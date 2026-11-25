package saru.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import saru.dao.PostDao;
import saru.model.Post;

/** POST /api/search/image (image) → 似た画像の投稿 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
@WebServlet("/api/search/image")
public class ImageSearchApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Integer userId = (Integer) req.getAttribute("apiUserId");
		try {
			if ("mizaru".equals(PostDao.accountType(userId))) {
				ApiJson.error(res, 403, "見ざるのアカウントは、画像検索を使えません");
				return;
			}
			Part part;
			try {
				part = req.getPart("image");
			} catch (IllegalStateException e) {
				ApiJson.error(res, 413, "画像は5MBまでです");
				return;
			}
			if (part == null || part.getSize() <= 0) {
				ApiJson.error(res, 400, "画像を選んでください");
				return;
			}
			BufferedImage img = ImageIO.read(part.getInputStream()); // 検索用の画像は、保存しない
			if (img == null) {
				ApiJson.error(res, 400, "画像として読み込めませんでした");
				return;
			}
			List<Post> posts = PostDao.searchByDHash(userId, ImageHash.dHash(img), 30);
			String viewerType = PostDao.accountType(userId);
			ApiJson.write(res, 200, ApiJson.posts(posts, userId, viewerType));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}