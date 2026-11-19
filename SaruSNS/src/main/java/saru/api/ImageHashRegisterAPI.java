package saru.api;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import saru.dao.ImageHashDAO;

/** 画像の指紋を登録  POST /api/image-hash  (multipart: post_id, image) ※確認・既存投稿の登録用 */
@WebServlet("/api/image-hash")
@MultipartConfig(maxFileSize = 4 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024)
public class ImageHashRegisterAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		try {
			int postId = Integer.parseInt(request.getParameter("post_id"));
			Part part = request.getPart("image");
			if (part == null || part.getSize() == 0) throw new IllegalArgumentException();
			long hash = DHash.compute(part.getInputStream().readAllBytes());

			if (new ImageHashDAO().save(postId, hash)) {
				response.setStatus(HttpServletResponse.SC_CREATED);
				out.print("{\"ok\":true,\"hash\":\"" + Long.toHexString(hash) + "\"}");
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				out.print("{\"error\":\"登録できませんでした(post_id が posts に無い、など)\"}");
			}
		} catch (IllegalArgumentException | IllegalStateException e) { // NumberFormatException も含む
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"post_id と image を指定してください\"}");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"この形式の画像は使えません\"}");
		}
	}
}