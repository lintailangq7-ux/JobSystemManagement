package saru.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import saru.Config;

@WebServlet("/media/*")
public class MediaServlet extends HttpServlet {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("m4a", "audio/mp4");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("aac", "audio/aac");
        MIME_TYPES.put("ogg", "audio/ogg");
    }

    private Path uploadRoot;

    @Override
    public void init() throws ServletException {
        try {
            uploadRoot = Config.load(getServletContext()).uploadDir;
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            res.sendError(404);
            return;
        }

        if (pathInfo.contains("..")) {
            res.sendError(403);
            return;
        }

        String relativePath = pathInfo.substring(1); // 最初の / を削除
        Path filePath = uploadRoot.resolve(relativePath);
        

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            res.sendError(404);
            return;
        }

    

        // 拡張子からMIMEタイプを判定
        String ext = getExtension(filePath.getFileName().toString()).toLowerCase();
        String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

        res.setContentType(mimeType);
        res.setContentLengthLong(Files.size(filePath));

        // ファイルを送信
        try (OutputStream out = res.getOutputStream()) {
            Files.copy(filePath, out);
        }
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i < 0 ? "" : filename.substring(i + 1);
    }
}
