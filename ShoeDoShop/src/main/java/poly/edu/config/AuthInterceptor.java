package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;
import poly.edu.service.AuthService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();

        // ── Whitelist: các route không cần đăng nhập ─────────────────
        if (isPublicUri(uri)) {
            return true;
        }

        // ── Kiểm tra đăng nhập (chỉ check 1 lần, bỏ duplicate) ──────
        if (authService.getCurrentUser() == null) {
            boolean autoLoggedIn = authService.autoLoginFromCookie();
            if (!autoLoggedIn) {
                // API request → trả 401, không redirect
                if (uri.startsWith("/api/")) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"success\":false,\"message\":\"Chưa đăng nhập\"}");
                    return false;
                }
                res.sendRedirect("/auth/login");
                return false;
            }
        }

        return checkRoleAccess(uri, req, res);
    }

    // ── Các URI không cần xác thực ────────────────────────────────────
    private boolean isPublicUri(String uri) {
        return uri.equals("/")
            || uri.equals("/customer/index")
            || uri.startsWith("/auth/")
            || uri.startsWith("/api/auth/")
            || uri.startsWith("/api/oauth2/")
            || uri.startsWith("/api/san-pham/")   // trang chủ public
            || uri.startsWith("/oauth2/")
            || uri.startsWith("/login/oauth2/")
            || uri.startsWith("/images/")
            || uri.startsWith("/anh/")
            || uri.startsWith("/error");
    }

    // ── Kiểm tra quyền theo role ──────────────────────────────────────
    private boolean checkRoleAccess(String uri, HttpServletRequest req, HttpServletResponse res) throws Exception {

        // /employee/** → phải là nhân viên
        if (uri.startsWith("/employee")) {
            if (!authService.isEmployee()) {
                res.sendRedirect("/auth/login");
                return false;
            }
            // /employee/dashboard → chỉ admin
            if ((uri.equals("/employee/dashboard") || uri.startsWith("/employee/dashboard/"))
                    && !authService.isAdmin()) {
                res.sendRedirect("/employee/products");
                return false;
            }
            return true;
        }

        // /customer/** (trừ /customer/index đã whitelist) → phải là khách hàng
        if (uri.startsWith("/customer")) {
            if (!authService.isCustomer()) {
                res.sendRedirect("/auth/login");
                return false;
            }
        }

        return true;
    }
}