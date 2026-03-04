package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import poly.edu.entity.*;
import poly.edu.dao.*;
import poly.edu.service.AuthService;
import poly.edu.service.CookieService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired UsersDAO usersDAO;
    @Autowired KhachHangDAO khachHangDAO;
    @Autowired QuanTriDAO quanTriDAO;
    @Autowired CookieService cookieService;
    @Autowired AuthService authService;
    @Autowired private PasswordEncoder passwordEncoder;
    
    @GetMapping("/auto-login")
    public ResponseEntity<Map<String, Object>> autoLogin() {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = authService.autoLoginFromCookie();
        
        if (success) {
            Users user = authService.getCurrentUser();
            response.put("success", true);
            response.put("user", getUserInfo(user));
        } else {
            response.put("success", false);
            response.put("message", "Không thể tự động đăng nhập");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Users user = authService.getCurrentUser();
            
            if (user != null) {
                response.put("success", true);
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("maUser", user.getMaUser());
                userData.put("mail", user.getMail());
                userData.put("userName", user.getUserName());
                userData.put("isActive", user.getIsActive());
                
                KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
                if (kh != null) {
                    userData.put("name", kh.getTenKH());
                    userData.put("role", "CUSTOMER");
                } else {
                    QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
                    if (qt != null) {
                        userData.put("name", qt.getTenQT());
                        userData.put("role", qt.getRole() ? "ADMIN" : "EMPLOYEE");
                    }
                }
                
                response.put("user", userData);
            } else {
                response.put("success", false);
                response.put("message", "No valid session");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking session: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String identifier = request.get("identifier");
        String pass = request.get("pass");
        boolean remember = Boolean.parseBoolean(request.get("remember"));
        
        String result = authService.loginWithIdentifier(identifier, pass, remember);
        
        if (!result.equals("OK")) {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        Users currentUser = authService.getCurrentUser();
        response.put("success", true);
        response.put("message", "Đăng nhập thành công");
        response.put("user", getUserInfo(currentUser));
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getUserInfo(Users user) {
        Map<String, Object> userInfo = new HashMap<>();
        if (user == null) return userInfo;
        
        userInfo.put("maUser", user.getMaUser());
        userInfo.put("mail", user.getMail());
        userInfo.put("userName", user.getUserName());
        userInfo.put("isActive", user.getIsActive());
        
        QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        
        if (qt != null) {
            userInfo.put("role", qt.getRole() ? "ADMIN" : "EMPLOYEE");
            userInfo.put("name", qt.getTenQT());
            userInfo.put("maQT", qt.getMaQT());
            userInfo.put("vaiTro", qt.getRole() ? "Admin" : "Nhân viên");
        } else if (kh != null) {
            userInfo.put("role", "CUSTOMER");
            userInfo.put("name", kh.getTenKH());
            userInfo.put("maKH", kh.getMaKH());
            userInfo.put("sdt", kh.getSdt());
        }
        
        return userInfo;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String mail = request.get("mail");
        String pass = request.get("pass");
        String fullname = request.get("fullname");
        String phone = request.get("phone");
        boolean remember = Boolean.parseBoolean(request.get("remember"));

        String userName = mail.split("@")[0];

        if (usersDAO.findByMail(mail) != null) {
            response.put("success", false);
            response.put("message", "Email đã tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Users user = new Users();
            user.setMail(mail);
            user.setUserName(userName);
            user.setPassWord(passwordEncoder.encode(pass));
            user.setIsActive(true);
            user.setCreateAt(new Date());
            
            Users savedUser = usersDAO.save(user);

            KhachHang khachHang = new KhachHang();
            khachHang.setTenKH(fullname);
            khachHang.setSdt(phone);
            khachHang.setUser(savedUser);
            khachHangDAO.save(khachHang);

            authService.loginWithIdentifier(mail, pass, remember);
            
            response.put("success", true);
            response.put("message", "Đăng ký thành công!");
            response.put("user", getUserInfo(savedUser));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đăng ký thất bại! Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();
        Users currentUser = authService.getCurrentUser();
        
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Chưa đăng nhập");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        response.put("success", true);
        response.put("user", getUserInfo(currentUser));
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse res) {
        authService.logout();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đăng xuất thành công");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String result = authService.forgotPassword(email);
        
        if (result.equals("OK")) {
            response.put("success", true);
            response.put("message", "Mật khẩu mới đã được gửi đến email của bạn");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");
        
        String result = authService.changePassword(email, currentPassword, newPassword, confirmPassword);
        
        if (result.equals("OK")) {
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
    }
}