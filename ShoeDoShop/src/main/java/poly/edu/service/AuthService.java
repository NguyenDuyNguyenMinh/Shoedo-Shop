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
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired UsersDAO usersDAO;
    @Autowired QuanTriDAO quanTriDAO;
    @Autowired KhachHangDAO khachHangDAO;
    @Autowired HttpSession session;
    @Autowired CookieService cookieService;
    @Autowired SessionService sessionService;
    @Autowired
    private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    public String loginWithIdentifier(String identifier, String pass, boolean remember) {
        Users user = null;
        
        if (identifier.contains("@")) {
            user = usersDAO.findByMail(identifier);
        } else {
            user = usersDAO.findByUserName(identifier);
        }

        if (user == null) {
            return "Sai tài khoản hoặc mật khẩu";
        }

        if (user.getIsActive() == null || !user.getIsActive()) {
            return "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên";
        }
        
        if (!passwordEncoder.matches(pass, user.getPassWord())) {
            return "Sai tài khoản hoặc mật khẩu";
        }

        QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());

        if (qt != null) {
            String role = qt.getRole() ? "ADMIN" : "EMPLOYEE";
            sessionService.set("userRole", role);
            sessionService.set("userName", qt.getTenQT());
        } else if (kh != null) {
            sessionService.set("userRole", "CUSTOMER");
            sessionService.set("userName", kh.getTenKH());
        } else {
            return "Tài khoản chưa được phân quyền";
        }
        
        sessionService.set("user", user);
        sessionService.set("userMail", user.getMail());
        sessionService.set("userName", user.getUserName());

        if (remember) {            
        	saveRememberMeCookie(user);
        } else {           
        	cookieService.remove("rememberMe");
        }

        return "OK";
    }

    private void saveRememberMeCookie(Users user) {
        try {            
            String userData = user.getMaUser() + ":" + user.getMail() + ":" + System.currentTimeMillis();
            String encodedData = Base64.getEncoder().encodeToString(userData.getBytes());
            
            cookieService.add("rememberMe", encodedData, 24 * 7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private Users getUserFromRememberCookie(String cookieValue) {
        try {
            
            byte[] decodedBytes = Base64.getDecoder().decode(cookieValue);
            String userData = new String(decodedBytes);
            
            String[] parts = userData.split(":");
            if (parts.length >= 2) {
                Integer userId = Integer.parseInt(parts[0]);
                String email = parts[1];

                Users user = usersDAO.findById(userId).orElse(null);

                if (user != null && user.getMail().equals(email) && user.getIsActive()) {
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean autoLoginFromCookie() {
        if (sessionService.get("user") != null) {
            return true;
        }
        
        String cookieValue = cookieService.getValue("rememberMe");
        
        if (cookieValue != null && !cookieValue.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(cookieValue);
                String userData = new String(decodedBytes);
                
                String[] parts = userData.split(":");
                if (parts.length >= 2) {
                    Integer userId = Integer.parseInt(parts[0]);
                    String email = parts[1];
                    Users user = usersDAO.findById(userId).orElse(null);
                    
                    if (user != null && user.getMail().equals(email) && user.getIsActive()) {
                        return performAutoLogin(user);
                    }
                }
                cookieService.remove("rememberMe");
                
            } catch (Exception e) {
                e.printStackTrace();
                cookieService.remove("rememberMe");
            }
        }
        
        return false;
    }
    
    private boolean performAutoLogin(Users user) {
        try {
            if (user.getIsActive() == null || !user.getIsActive()) {
                return false;
            }
            
            QuanTri qt = quanTriDAO.findByUser_MaUser(user.getMaUser());
            KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());

            if (qt != null) {
                String role = qt.getRole() ? "ADMIN" : "EMPLOYEE";
                sessionService.set("userRole", role);
                sessionService.set("userName", qt.getTenQT());
            } else if (kh != null) {
                sessionService.set("userRole", "CUSTOMER");
                sessionService.set("userName", kh.getTenKH());
            } else {
                return false;
            }
            
            sessionService.set("user", user);
            sessionService.set("userMail", user.getMail());

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String register(Users user, KhachHang khachHang) {
        if (usersDAO.findByMail(user.getMail()) != null) {
            return "Email đã tồn tại";
        }

        user.setIsActive(true);
        user.setCreateAt(new Date());
        user.setPassWord(passwordEncoder.encode(user.getPassWord()));
        
        Users savedUser = usersDAO.save(user);

        khachHang.setUser(savedUser);
        khachHangDAO.save(khachHang);
        
        return "OK";
    }

    public void logout() {
    	
    	cookieService.remove("rememberMe");
        sessionService.remove("userRole");
        sessionService.remove("userName");
        sessionService.remove("user");
        sessionService.remove("userMail");
     
        session.invalidate();
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

    public Users getCurrentUser() {
        return sessionService.get("user");
    }
    
    public String forgotPassword(String email) {
        try {
            Users user = usersDAO.findByMail(email);
            if (user == null) {
                return "Email không tồn tại trong hệ thống";
            }

            String newPassword = generateRandomPassword();
            user.setPassWord(passwordEncoder.encode(newPassword));
            usersDAO.save(user);

            KhachHang khachHang = khachHangDAO.findByUser_MaUser(user.getMaUser());
            String fullname = (khachHang != null) ? khachHang.getTenKH() : "Quý khách";

            sendPasswordResetEmail(email, fullname, newPassword);

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi xảy ra. Vui lòng thử lại sau.";
        }
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
    
    public String changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        try {
            Users user = usersDAO.findByMail(email);
            if (user == null) {
                return "Email không tồn tại trong hệ thống";
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassWord())) {
                return "Mật khẩu hiện tại không đúng";
            }

            if (!newPassword.equals(confirmPassword)) {
                return "Mật khẩu mới và xác nhận mật khẩu không khớp";
            }

            user.setPassWord(passwordEncoder.encode(newPassword));
            usersDAO.save(user);

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }

    
    public String processGoogleLogin(String email, String name) {
        try {
            Users user = usersDAO.findByMail(email);
            
            if (user == null) {
                return "Tài khoản Google không tồn tại. Vui lòng đăng ký trước khi đăng nhập.";
            }
            
            if (user.getIsActive() == null || !user.getIsActive()) {
                return "Tài khoản Google đã bị khóa";
            }
            
            return this.autoLoginGoogleUser(user, name);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi xử lý đăng nhập Google: " + e.getMessage();
        }
    }
    
    public String processGoogleLoginNewUser(String email, String name, String password) {
        try {
            Users user = usersDAO.findByMail(email);
            
            if (user != null) {
                if (user.getIsActive() == null || !user.getIsActive()) {
                    return "Tài khoản Google đã bị khóa";
                }
                return this.autoLoginGoogleUser(user, name);
            }
            
            user = new Users();
            user.setMail(email);
            user.setUserName(email.split("@")[0]);
            user.setPassWord(passwordEncoder.encode(password)); 
            user.setIsActive(true);
            user.setCreateAt(new Date());
            user = usersDAO.save(user);

            KhachHang khachHang = new KhachHang();
            khachHang.setTenKH(name);
            khachHang.setSdt("N/A");
            khachHang.setUser(user);
            khachHangDAO.save(khachHang);
            
            return this.autoLoginGoogleUser(user, name);

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi xử lý đăng nhập Google: " + e.getMessage();
        }
    }
    
    
    private String autoLoginGoogleUser(Users user, String name) {
        try {
            KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());

            sessionService.set("userRole", "CUSTOMER");
            sessionService.set("userName", kh != null ? kh.getTenKH() : name);
            sessionService.set("user", user);
            sessionService.set("userMail", user.getMail());
            sessionService.set("isGoogleUser", true);
           
            saveRememberMeCookie(user);
            
            return "OK";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi đăng nhập tự động: " + e.getMessage();
        }
    }
    
    public boolean isGoogleUser() {
        return sessionService.get("isGoogleUser") != null && 
               (boolean) sessionService.get("isGoogleUser");
    }
    
}