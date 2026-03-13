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
    
    private Map<String, RegistrationInfo> registrationConfirmations = new HashMap<>();
    private Map<String, ForgotPasswordInfo> forgotPasswordConfirmations = new HashMap<>();
    
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

    // ==================== REGISTER WITH EMAIL CONFIRMATION ====================
    public Map<String, Object> sendRegister(Map<String, String> request) {
        String mail = request.get("mail");
        String pass = request.get("pass");
        String fullname = request.get("fullname");
        String phone = request.get("phone");
        
        if (usersDAO.findByMail(mail) != null) {
            return error("Email đã tồn tại trong hệ thống!");
        }

        String confirmationCode = generateRandomCode(6);
        
        RegistrationInfo info = new RegistrationInfo();
        info.setMail(mail);
        info.setPass(pass);
        info.setFullname(fullname);
        info.setPhone(phone);
        info.setConfirmationCode(confirmationCode);
        info.setExpiryTime(System.currentTimeMillis() + 10 * 60 * 1000); 
        
        registrationConfirmations.put(mail, info);
        sendRegistrationConfirmationEmail(mail, fullname, confirmationCode);
        
        return success("Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra email.");
    }
    
    public Map<String, Object> completeRegister(Map<String, String> request) {
        String mail = request.get("mail");
        String confirmationCode = request.get("confirmationCode");

        RegistrationInfo info = registrationConfirmations.get(mail);
        
        if (info == null) {
            return error("Không tìm thấy yêu cầu đăng ký. Vui lòng đăng ký lại.");
        }
        
        if (System.currentTimeMillis() > info.getExpiryTime()) {
            registrationConfirmations.remove(mail);
            return error("Mã xác nhận đã hết hạn. Vui lòng đăng ký lại.");
        }
        
        if (!info.getConfirmationCode().equals(confirmationCode)) {
            return error("Mã xác nhận không chính xác. Vui lòng kiểm tra lại.");
        }

        Users user = new Users();
        user.setMail(info.getMail());
        user.setUserName(info.getMail().split("@")[0]);
        user.setPassWord(passwordEncoder.encode(info.getPass()));
        user.setIsActive(true);
        user.setCreateAt(new Date());
        user = usersDAO.save(user);
        
        KhachHang kh = new KhachHang();
        kh.setTenKH(info.getFullname());
        kh.setSdt(info.getPhone());
        kh.setUser(user);
        khachHangDAO.save(kh);
        
        registrationConfirmations.remove(mail);
        
        return success("Đăng ký tài khoản thành công!");
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
        
        if (user == null || !user.getIsActive()) {
            return error("Tài khoản đã bị khóa");
        }
                  
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

    // ==================== FORGOT PASSWORD WITH EMAIL CONFIRMATION ====================
    public Map<String, Object> sendForgotPass(Map<String, String> request) {
        String email = request.get("email");
        
        Users user = usersDAO.findByMail(email);
        if (user == null) {
            return error("Email không tồn tại trong hệ thống!");
        }

        String confirmationCode = generateRandomCode(6);
        String newPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(newPassword);

        ForgotPasswordInfo info = new ForgotPasswordInfo();
        info.setEmail(email);
        info.setConfirmationCode(confirmationCode);
        info.setNewPassword(encodedPassword);
        info.setNewPasswordRaw(newPassword);
        info.setExpiryTime(System.currentTimeMillis() + 10 * 60 * 1000);
        
        forgotPasswordConfirmations.put(email, info);

        String fullname = Optional.ofNullable(khachHangDAO.findByUser_MaUser(user.getMaUser()))
            .map(KhachHang::getTenKH).orElse("Quý khách");
        
        sendForgotPasswordConfirmationEmail(email, fullname, confirmationCode);
        
        return success("Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra email.");
    }
    
    public Map<String, Object> confirmForgotPass(Map<String, String> request) {
        String email = request.get("email");
        String confirmationCode = request.get("confirmationCode");

        ForgotPasswordInfo info = forgotPasswordConfirmations.get(email);
        
        if (info == null) {
            return error("Không tìm thấy yêu cầu khôi phục mật khẩu. Vui lòng thử lại.");
        }
        
        if (System.currentTimeMillis() > info.getExpiryTime()) {
        	forgotPasswordConfirmations.remove(email);
            return error("Mã xác nhận đã hết hạn. Vui lòng thử lại.");
        }
        
        if (!info.getConfirmationCode().equals(confirmationCode)) {
            return error("Mã xác nhận không chính xác. Vui lòng kiểm tra lại.");
        }
        Users user = usersDAO.findByMail(email);
        if (user == null) {
            return error("Email không tồn tại trong hệ thống!");
        }
        
        user.setPassWord(info.getNewPassword());
        usersDAO.save(user);
        String fullname = Optional.ofNullable(khachHangDAO.findByUser_MaUser(user.getMaUser()))
            .map(KhachHang::getTenKH).orElse("Quý khách");
        
        sendPasswordResetEmail(email, fullname, info.getNewPasswordRaw());
        forgotPasswordConfirmations.remove(email);
        
        return success("Mật khẩu mới đã được gửi đến email của bạn. Vui lòng kiểm tra email.");
    }
    // ==================== PASSWORD ====================

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
    	try { 
	        QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
	        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
	
	        if (qt != null) {
	            sessionService.set("userRole", qt.getRole() ? "ADMIN" : "EMPLOYEE");
	            sessionService.set("userName", qt.getTenQT());
	            sessionService.set("user", user);
	            sessionService.set("isGoogleUser", true);
	        } else if (kh != null) {
	            sessionService.set("userRole", "CUSTOMER");
	            sessionService.set("userName", kh.getTenKH());
	            sessionService.set("user", user);
	            sessionService.set("isGoogleUser", true);
	        }
	        
	        sessionService.set("user", user);
	        sessionService.set("userMail", user.getMail());
	        
	        if (remember) saveRememberMe(user);
	        
	        return getUserInfo(user);
    	}catch (Exception e){
    		e.printStackTrace();
    	}      
    	return null;  
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
    
    private String generateRandomCode(int length) {
        String chars = "0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
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
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
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
    
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
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
    // ==================== EMAIL SENDING METHODS ====================
    private void sendRegistrationConfirmationEmail(String email, String fullname, String confirmationCode) {
        try {
            String subject = "SHOEDO SHOP - Xác nhận đăng ký tài khoản";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".code-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px;  }"
                    + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                    + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'>"
                    + "<h2>ShoeDo Shop - Xác nhận đăng ký tài khoản</h2>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản tại ShoeDo Shop.</p>"
                    + "<p>Vui lòng nhập mã xác nhận bên dưới để hoàn tất quá trình đăng ký:</p>"
                    + "<div class='code-box'>"
                    + confirmationCode
                    + "</div>"
                    + "<p>Mã xác nhận này có hiệu lực trong <strong>10 phút</strong>.</p>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý:</strong> Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }
    }
    
    private void sendForgotPasswordConfirmationEmail(String email, String fullname, String confirmationCode) {
        try {
            String subject = "SHOEDO SHOP - Xác nhận khôi phục mật khẩu";
            
            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                    + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                    + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                    + ".content { padding: 20px; background: #f9f9f9; }"
                    + ".code-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px;  }"
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
                    + "<p>Vui lòng nhập mã xác nhận bên dưới để xác nhận yêu cầu:</p>"
                    + "<div class='code-box'>"
                    + confirmationCode
                    + "</div>"
                    + "<p>Mã xác nhận này có hiệu lực trong <strong>10 phút</strong>.</p>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý:</strong> Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div class='footer'>"
                    + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                    + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }
    }
    
    
    // ==================== HELPER CLASSES ====================
    public static class RegistrationInfo {
        private String mail;
        private String pass;
        private String fullname;
        private String phone;
        private String confirmationCode;
        private long expiryTime;
        
        public String getMail() { return mail; }
        public void setMail(String mail) { this.mail = mail; }
        
        public String getPass() { return pass; }
        public void setPass(String pass) { this.pass = pass; }
        
        public String getFullname() { return fullname; }
        public void setFullname(String fullname) { this.fullname = fullname; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getConfirmationCode() { return confirmationCode; }
        public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }
        
        public long getExpiryTime() { return expiryTime; }
        public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }
    }
    
    public static class ForgotPasswordInfo {
        private String email;
        private String confirmationCode;
        private String newPassword;
        private String newPasswordRaw;
        private long expiryTime;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getConfirmationCode() { return confirmationCode; }
        public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        
        public String getNewPasswordRaw() { return newPasswordRaw; }
        public void setNewPasswordRaw(String newPasswordRaw) { this.newPasswordRaw = newPasswordRaw; }
        
        public long getExpiryTime() { return expiryTime; }
        public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }
    }
    
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredConfirmations() {
        long now = System.currentTimeMillis();
        int beforeReg = registrationConfirmations.size();
        int beforeForgot = forgotPasswordConfirmations.size();
        
        registrationConfirmations.entrySet().removeIf(entry ->  {
            boolean expired = entry.getValue().getExpiryTime() < now;
            if (expired) {
                System.out.println("Xóa OTP đăng ký hết hạn của: " + entry.getKey());
            }
            return expired;
        });
 
        forgotPasswordConfirmations.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getExpiryTime() < now;
            if (expired) {
                System.out.println("Xóa OTP quên mật khẩu hết hạn của: " + entry.getKey());
            }
            return expired;
        });
        
//        System.out.println(String.format(
//            "Cleanup: Đăng ký: %d → %d, Quên MK: %d → %d",
//            beforeReg, registrationConfirmations.size(), beforeForgot, forgotPasswordConfirmations.size()
//    	));
    }
}