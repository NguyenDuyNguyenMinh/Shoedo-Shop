package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

import jakarta.servlet.http.HttpSession;
import poly.edu.dao.*;
import poly.edu.entity.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired private UsersDAO usersDAO;
    @Autowired private QuanTriDAO quanTriDAO;
    @Autowired private KhachHangDAO khachHangDAO;
    @Autowired private SessionService sessionService;
    @Autowired private CookieService cookieService;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private GioHangDAO gioHangDAO;
    // ==================== LOGIN ====================
    public Map<String, Object> login(Map<String, String> request) {
        String identifier = request.get("identifier");
        String pass = request.get("pass");
        boolean remember = Boolean.parseBoolean(request.get("remember"));
        
        Users user = identifier.contains("@") ? 
            usersDAO.findByMail(identifier) : 
            usersDAO.findByUserName(identifier);
        
        if (user == null) return error("Sai tài khoản hoặc mật khẩu");
        if (!user.getIsActive()) return error("Tài khoản đã bị khóa");
        if (!passwordEncoder.matches(pass, user.getPassWord())) return error("Sai tài khoản hoặc mật khẩu");
        
        return success(doLogin(user, remember));
    }

    public Map<String, Object> autoLogin() {
        Users user = getCurrentUser();
        if (user == null) {
            autoLoginFromCookie();
            user = getCurrentUser();
        }
        return user != null ? success(getUserInfo(user)) : error("Không thể tự động đăng nhập");
    }

    public Map<String, Object> checkSession() {
        Users user = getCurrentUser();
        if (user == null) autoLoginFromCookie();
        user = getCurrentUser();
        return user != null ? success(getUserInfo(user)) : error("No valid session");
    }

    public Map<String, Object> getCurrentUserInfo() {
        Users user = getCurrentUser();
        if (user == null) autoLoginFromCookie();
        user = getCurrentUser();
        if (user != null) {
            Map<String, Object> userInfo = getUserInfo(user);
            
            Integer cartCount = getCartCount(user);
            userInfo.put("cartCount", cartCount);
            
            return success(userInfo);
        }
        return user != null ? success(getUserInfo(user)) : error("Chưa đăng nhập");
    }
    
    public Integer getCartCount(Users user) {
        if (user == null) return 0;
        
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (khachHang == null) return 0;
        
        List<GioHang> cartItems = gioHangDAO.findByKhachHang_MaKH(khachHang.getMaKH());
        
        return cartItems.size();
    }
    
    // ==================== REGISTER ====================
    public Map<String, Object> register(Map<String, String> request) {
        String mail = request.get("mail");
        String pass = request.get("pass");
        String fullname = request.get("fullname");
        String phone = request.get("phone");
        boolean remember = Boolean.parseBoolean(request.get("remember"));

        if (usersDAO.findByMail(mail) != null) return error("Email đã tồn tại!");

        Users user = new Users();
        user.setMail(mail);
        user.setUserName(mail.split("@")[0]);
        user.setPassWord(passwordEncoder.encode(pass));
        user.setIsActive(true);
        user.setCreateAt(new Date());
        user = usersDAO.save(user);

        KhachHang kh = new KhachHang();
        kh.setTenKH(fullname);
        kh.setSdt(phone);
        kh.setUser(user);
        khachHangDAO.save(kh);

        return success(doLogin(user, remember));
    }

    // ==================== GOOGLE ====================
    public Map<String, Object> handleGoogleCallback(String email, String name) {
        Users user = usersDAO.findByMail(email);
        
        if (user == null) {
            return Map.of("success", true, "requirePassword", true, "email", email, "name", name != null ? name : "Google User");
        }
        
        if (!user.getIsActive()) return error("Tài khoản đã bị khóa");
        
        return success(doLogin(user, false));
    }

    public Map<String, Object> googleLogin(Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name");
        
        Users user = usersDAO.findByMail(email);
        if (user == null || !user.getIsActive()) return error("Đăng nhập thất bại");
        
        return success(doLogin(user, false));
    }

    public Map<String, Object> googleLoginNew(Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name");
        String password = request.get("password");
        
        Users user = usersDAO.findByMail(email);
        
        if (user == null) {
            user = new Users();
            user.setMail(email);
            user.setUserName(email.split("@")[0]);
            user.setPassWord(passwordEncoder.encode(password));
            user.setIsActive(true);
            user.setCreateAt(new Date());
            user = usersDAO.save(user);

            KhachHang kh = new KhachHang();
            kh.setTenKH(name != null ? name : "Google User");
            kh.setSdt("N/A");
            kh.setUser(user);
            khachHangDAO.save(kh);
        }
        
        return success(doLogin(user, false));
    }

    // ==================== PASSWORD ====================
    public Map<String, Object> forgotPassword(Map<String, String> request) {
        String email = request.get("email");
        Users user = usersDAO.findByMail(email);
        
        if (user == null) return error("Email không tồn tại");
        
        String newPass = generateRandomPassword();
        user.setPassWord(passwordEncoder.encode(newPass));
        usersDAO.save(user);
        
        String fullname = Optional.ofNullable(khachHangDAO.findByUser_MaUser(user.getMaUser()))
            .map(KhachHang::getTenKH).orElse("Quý khách");
        
        sendPasswordResetEmail(email, fullname, newPass);
        
        return success("Mật khẩu mới đã được gửi đến email");
    }

    public Map<String, Object> changePassword(Map<String, String> request) {
        String email = request.get("email");
        String current = request.get("currentPassword");
        String newPass = request.get("newPassword");
        String confirm = request.get("confirmPassword");
        
        Users user = usersDAO.findByMail(email);
        if (user == null) return error("Email không tồn tại");
        if (!passwordEncoder.matches(current, user.getPassWord())) return error("Mật khẩu hiện tại không đúng");
        if (!newPass.equals(confirm)) return error("Mật khẩu mới không khớp");
        
        user.setPassWord(passwordEncoder.encode(newPass));
        usersDAO.save(user);
        
        return success("Đổi mật khẩu thành công");
    }

    // ==================== LOGOUT ====================
    public Map<String, Object> logout() {
        cookieService.remove("rememberMe");
        sessionService.remove("userRole");
        sessionService.remove("userName");
        sessionService.remove("user");
        sessionService.remove("userMail");
        sessionService.remove("isGoogleUser");
        return success("Đăng xuất thành công");
    }

    // ==================== PRIVATE METHODS ====================
    private Map<String, Object> doLogin(Users user, boolean remember) {
        QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());

        if (qt != null) {
            sessionService.set("userRole", qt.getRole() ? "ADMIN" : "EMPLOYEE");
            sessionService.set("userName", qt.getTenQT());
        } else if (kh != null) {
            sessionService.set("userRole", "CUSTOMER");
            sessionService.set("userName", kh.getTenKH());
        }
        
        sessionService.set("user", user);
        sessionService.set("userMail", user.getMail());

        if (remember) saveRememberMe(user);
        
        return getUserInfo(user);
    }

    private void saveRememberMe(Users user) {
        String data = Base64.getEncoder().encodeToString(
            (user.getMaUser() + ":" + user.getMail()).getBytes()
        );
        cookieService.add("rememberMe", data, 24 * 7);
    }

    private Map<String, Object> getUserInfo(Users user) {
        Map<String, Object> info = new HashMap<>();
        info.put("maUser", user.getMaUser());
        info.put("mail", user.getMail());
        info.put("userName", user.getUserName());
        info.put("isActive", user.getIsActive());

        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh != null) {
            info.put("role", "CUSTOMER");
            info.put("name", kh.getTenKH());
            info.put("maKH", kh.getMaKH());
            info.put("sdt", kh.getSdt());
            return info;
        }

        QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
        if (qt != null) {
            info.put("role", qt.getRole() ? "ADMIN" : "EMPLOYEE");
            info.put("name", qt.getTenQT());
            info.put("maQT", qt.getMaQT());
            info.put("vaiTro", qt.getRole() ? "Admin" : "Nhân viên");
        }
        return info;
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void sendPasswordResetEmail(String email, String fullname, String newPassword) {
        try {
        	String subject = "SHOEDO SHOP - Khôi phục mật khẩu";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".password-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px; }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>ShoeDo Shop - Khôi phục mật khẩu</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>"
                    + "<p>Mật khẩu mới của bạn là:</p>"
                    + "<div class='password-box'>"
                    + newPassword
                    + "</div>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý quan trọng:</strong></p>"
                    + "<p>• Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi truy cập hệ thống</p>"
                    + "<p>• Không chia sẻ mật khẩu này với bất kỳ ai</p>"
                    + "</div>"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2025 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }
    
    // ==================== GETTERS ====================
    public Users getCurrentUser() {
        return sessionService.get("user");
    }
    
    public boolean isEmployee() {
        String role = sessionService.get("userRole");
        return role != null && (role.equals("ADMIN") || role.equals("EMPLOYEE"));
    }

    public boolean isAdmin() {
        String role = sessionService.get("userRole");
        return role != null && role.equals("ADMIN");
    }

    public boolean isCustomer() {
        String role = sessionService.get("userRole");
        return role != null && role.equals("CUSTOMER");
    }
    
    public String getCurrentUserRole() {
        return sessionService.get("userRole");
    }
    
    public String getCurrentUserMail() {
        return sessionService.get("userMail");
    }

    public boolean isGoogleUser() {
        return sessionService.get("isGoogleUser") != null && 
               (boolean) sessionService.get("isGoogleUser");
    }
    
    // ==================== UTILS ====================
    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "user", data);
    }
    
    private Map<String, Object> success(String message) {
        return Map.of("success", true, "message", message);
    }
    
    private Map<String, Object> error(String message) {
        return Map.of("success", false, "message", message);
    }
    
    
    // ==================== PUBLIC ====================
    public boolean autoLoginFromCookie() {
        String cookie = cookieService.getValue("rememberMe");
        if (cookie == null) return false;
        
        try {
            String[] parts = new String(Base64.getDecoder().decode(cookie)).split(":");
            if (parts.length < 2) return false;
            
            Users user = usersDAO.findById(Integer.parseInt(parts[0])).orElse(null);
            if (user != null && user.getMail().equals(parts[1]) && user.getIsActive()) {
                doLogin(user, false);
                return true;
            }
        } catch (Exception e) {
            cookieService.remove("rememberMe");
        }
        return false;
    }

    public String changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        Users user = usersDAO.findByMail(email);
        if (user == null) return "Email không tồn tại";
        if (!passwordEncoder.matches(currentPassword, user.getPassWord())) return "Mật khẩu hiện tại không đúng";
        if (!newPassword.equals(confirmPassword)) return "Mật khẩu mới không khớp";
        
        user.setPassWord(passwordEncoder.encode(newPassword));
        usersDAO.save(user);
        
        return "OK";
    }
    
}