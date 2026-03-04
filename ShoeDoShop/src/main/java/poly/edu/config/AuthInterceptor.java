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
        
        if (uri.startsWith("/auth/") || 
            uri.startsWith("/api/auth/") ||
            uri.startsWith("/api/nhapkho/") ||
            uri.startsWith("/error") ||
            uri.startsWith("/api/oauth2/") ||
            uri.startsWith("/oauth2/") ||
            uri.startsWith("/images/") ||
            uri.startsWith("/anh/") ||
            uri.equals("/") ||
            uri.equals("/customer/index")) {
            return true;
        }
        
        if (req.getSession().getAttribute("user") == null) {
            boolean autoLoggedIn = authService.autoLoginFromCookie();
            if (!autoLoggedIn) {
            	if (uri.startsWith("/api/")) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    res.sendRedirect("/auth/login");
                }
                return false;
            }
        }
        
        if (authService.getCurrentUser() == null) {
            boolean autoLoggedIn = authService.autoLoginFromCookie();
            if (!autoLoggedIn) {
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
            
            if (uri.equals("/employee/dashboard") && !authService.isAdmin()) {
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