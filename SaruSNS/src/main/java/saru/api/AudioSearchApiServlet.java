package saru.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import saru.dao.PostDao;
import saru.model.Post;

/** POST /api/search/audio (audio = WAV) → 音色が近い音声の投稿 */
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
@WebServlet("/api/search/audio")
public class AudioSearchApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Integer userId = (Integer) req.getAttribute("apiUserId");
		try {
			if ("kikazaru".equals(PostDao.accountType(userId))) {
				ApiJson.error(res, 403, "聞かざるのアカウントは、音声検索を使えません");
				return;
			}
			Part part;
			try {
				part = req.getPart("audio");
			} catch (IllegalStateException e) {
				ApiJson.error(res, 413, "音声は10MBまでです");
				return;
			}
			if (part == null || part.getSize() <= 0) {
				ApiJson.error(res, 400, "音声を送ってください");
				return;
			}
			float[] query = AudioFeature.mfcc(part.getInputStream().readAllBytes()); // 検索用の音は、保存しない
			if (query == null) {
				ApiJson.error(res, 400, "16bitのWAVで、1秒以上の音を送ってください");
				return;
			}
			List<PostDao.FeatureRow> rows = PostDao.audioFeatures(userId);
			if (rows.isEmpty()) {
				ApiJson.write(res, 200, "[]");
				return;
			}
			List<Integer> ids = AudioFeature.rank(query,
					rows.stream().map(PostDao.FeatureRow::postId).toList(),
					rows.stream().map(r -> AudioFeature.parse(r.vec())).toList(), 30);
			String viewerType = PostDao.accountType(userId);
			List<Post> resultPosts = PostDao.findInOrder(userId, ids);
			ApiJson.write(res, 200, ApiJson.posts(resultPosts, userId, viewerType));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}