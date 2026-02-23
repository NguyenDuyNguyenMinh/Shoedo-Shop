package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import poly.edu.service.AuthService;
import poly.edu.entity.Users;
import poly.edu.entity.KhachHang;
import poly.edu.entity.QuanTri;
import poly.edu.dao.UsersDAO;
import poly.edu.dao.KhachHangDAO;
import poly.edu.dao.QuanTriDAO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UsersDAO usersDAO;
    
    @Autowired
    private KhachHangDAO khachHangDAO;
    
    @Autowired
    private QuanTriDAO quanTriDAO;

    @GetMapping("/google/callback")
    public ResponseEntity<Map<String, Object>> googleCallback(OAuth2AuthenticationToken authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            response.put("success", false);
            response.put("message", "Authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String email = authentication.getPrincipal().getAttribute("email");
            String name = authentication.getPrincipal().getAttribute("name");
            
            if (email == null) {
                response.put("success", false);
                response.put("message", "Email not provided by Google");
                return ResponseEntity.badRequest().body(response);
            }
            
            Users existingUser = usersDAO.findByMail(email);
            
            if (existingUser == null) {
                response.put("success", true);
                response.put("requirePassword", true);
                response.put("email", email);
                response.put("name", name != null ? name : "Google User");
                response.put("message", "Vui lòng nhập mật khẩu cho tài khoản Google của bạn");
                return ResponseEntity.ok(response);
            } else {
                String result = authService.processGoogleLogin(email, name != null ? name : "Google User");
                
                if (result.equals("OK")) {
                    response.put("success", true);
                    response.put("requirePassword", false);
                    response.put("message", "Google login successful");
                    response.put("email", email);
                    response.put("name", name);
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", result);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error processing Google login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/google-login")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String name = request.get("name");
        
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        
        String result = authService.processGoogleLogin(email, name != null ? name : "Google User");
        
        if (result.equals("OK")) {
            Users currentUser = authService.getCurrentUser();
            response.put("success", true);
            response.put("message", "Đăng nhập Google thành công");
            response.put("user", getUserInfo(currentUser));
            response.put("isGoogleUser", true);

            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/google-login-newuesr")
    public ResponseEntity<Map<String, Object>> googleLoginNewUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String name = request.get("name");
        String password = request.get("password");
        
        if (email == null || email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (password == null || password.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mật khẩu không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        
        String result = authService.processGoogleLoginNewUser(email, name != null ? name : "Google User", password);
        
        if (result.equals("OK")) {
            Users currentUser = authService.getCurrentUser();
            response.put("success", true);
            response.put("message", "Đăng nhập Google thành công");
            response.put("user", getUserInfo(currentUser));
            response.put("isGoogleUser", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    private Map<String, Object> getUserInfo(Users user) {
        Map<String, Object> userInfo = new HashMap<>();
        if (user == null) return userInfo;
        
        userInfo.put("maUser", user.getMaUser());
        userInfo.put("mail", user.getMail());
        userInfo.put("userName", user.getUserName());
        userInfo.put("isActive", user.getIsActive());
        
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
        
        if (kh != null) {
            userInfo.put("role", "CUSTOMER");
            userInfo.put("name", kh.getTenKH());
            userInfo.put("maKH", kh.getMaKH());
            userInfo.put("sdt", kh.getSdt());
        } else if (qt != null) {
            userInfo.put("role", qt.getRole() ? "ADMIN" : "EMPLOYEE");
            userInfo.put("name", qt.getTenQT());
            userInfo.put("maQT", qt.getMaQT());
            userInfo.put("vaiTro", qt.getRole() ? "Admin" : "Nhân viên");
        }
        
        return userInfo;
    }
    
}