package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;
import poly.edu.service.AuthService;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired AuthService authService;

    private static final List<String> PUBLIC_PREFIXES = List.of(
        "/auth/", "/api/auth/", "/api/oauth2/", "/oauth2/", 
        "/images/", "/anh/", "/api/sanpham/flash-sales", 
        "/api/sanpham/noi-bat", "/api/sanpham/ban-chay", 
        "/api/sanpham/trang-chu", "/api/sanpham/detail", 
        "/api/san-pham/", "/api/danh-gia/", "/api/public/", "/api/chat/"
    );

    private static final List<String> PUBLIC_EXACT = List.of(
        "/", "/customer/index", "/customer/chinhsach", "/customer/sanpham"
    );

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();
        
        if (PUBLIC_EXACT.contains(uri) || PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith)) {
            return true;
        }
        
        if (req.getSession().getAttribute("user") == null) {
            if (uri.startsWith("/employee") || uri.startsWith("/api/employee")) {
                res.sendRedirect("/auth/login");
                return false;
            }
            if (!authService.autoLoginFromCookie()) {
                res.sendRedirect("/auth/login");
                return false;
            }
        }

        return checkRoleAccess(uri, req, res);
    }
    
    private boolean checkRoleAccess(String uri, HttpServletRequest req, HttpServletResponse res) throws Exception {
        if (uri.startsWith("/employee")) {
            if (!authService.isEmployee()) {
                res.sendRedirect("/auth/login");
                return false;
            }
            
            Object user = req.getSession().getAttribute("user");
            if (user == null) {
                res.sendRedirect("/auth/login");
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
                res.sendRedirect("/auth/login");
                return false;
            }
        }
        
        return true;
    }
}
