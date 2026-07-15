package filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class Login implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false); // 既存セッションのみ取得

        String requestURI = req.getRequestURI();

        // ログイン関連ページはフィルターを通過させる
        if (requestURI.endsWith("/Login.jsp") || 
            requestURI.endsWith("/LoginServlet") ||
            requestURI.endsWith("/login")) {
            
            chain.doFilter(request, response);
            return;
        }

        // セッションにログイン情報があるかチェック
        boolean isLoggedIn = false;
        
        if (session != null) {
            // 先生ID または 生徒ID のどちらかがセッションにあればOK
            if (session.getAttribute("userId") != null) {
                isLoggedIn = true;
            }
        }

        if (isLoggedIn) {
            // ログイン済み → 次の処理へ
            chain.doFilter(request, response);
        } else {
            // 未ログイン → ログインページへリダイレクト
            res.sendRedirect(req.getContextPath() + "/Login.jsp");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初期化処理（必要なら）
    }

    @Override
    public void destroy() {
        // 終了処理（必要なら）
    }
}
