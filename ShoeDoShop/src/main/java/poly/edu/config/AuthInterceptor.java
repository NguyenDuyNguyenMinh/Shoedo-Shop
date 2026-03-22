package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;
import poly.edu.service.AuthService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();

        // ── 1. Public paths: cho phép qua ngay ───────────────────────
        if (uri.startsWith("/auth/") ||
            uri.startsWith("/api/auth/") ||
            uri.startsWith("/api/oauth2/") ||
            uri.startsWith("/oauth2/") ||
            uri.startsWith("/images/") ||
            uri.startsWith("/anh/") ||
            uri.startsWith("/api/sanpham/flash-sales") ||
            uri.startsWith("/api/sanpham/noi-bat") ||
            uri.startsWith("/api/sanpham/ban-chay") ||
            uri.startsWith("/api/sanpham/trang-chu") ||
            uri.startsWith("/api/sanpham/detail") ||
            uri.startsWith("/api/san-pham/") ||
            uri.startsWith("/api/danh-gia/") ||
            uri.startsWith("/api/public/") ||
            uri.equals("/") ||
            uri.equals("/customer/index") ||
            uri.equals("/customer/chinhsach") ||
            uri.equals("/customer/sanpham")) {
            return true;
        }

        // ── 2. Xác thực: thử auto-login từ cookie trước ─────────────
        if (authService.getCurrentUser() == null) {
            boolean autoLoggedIn = authService.autoLoginFromCookie();
            if (!autoLoggedIn) {
                // API request → trả 401 JSON, page request → redirect
                if (isApiRequest(uri)) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"success\":false,\"message\":\"Chưa đăng nhập\"}");
                } else {
                    res.sendRedirect("/auth/login");
                }
                return false;
            }
        }

        return checkRoleAccess(uri, req, res);
    }

    private boolean checkRoleAccess(String uri, HttpServletRequest req, HttpServletResponse res) throws Exception {
        if (uri.startsWith("/employee")) {
            if (!authService.isEmployee()) {
                if (isApiRequest(uri)) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"success\":false,\"message\":\"Không có quyền truy cập\"}");
                } else {
                    res.sendRedirect("/auth/login");
                }
                return false;
            }

            if ((uri.equals("/employee/dashboard") || uri.startsWith("/employee/dashboard/"))
                    && !authService.isAdmin()) {
                res.sendRedirect("/employee/products");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/customer") && !uri.equals("/customer/index")) {
            if (!authService.isCustomer()) {
                if (isApiRequest(uri)) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"success\":false,\"message\":\"Tài khoản không phải khách hàng\"}");
                } else {
                    res.sendRedirect("/auth/login");
                }
                return false;
            }
        }

        return true;
    }

    /**
     * API request = path bắt đầu bằng /api/
     * Trả về JSON thay vì HTML redirect để axios xử lý đúng.
     */
    private boolean isApiRequest(String uri) {
        return uri.startsWith("/api/");
    }
}
