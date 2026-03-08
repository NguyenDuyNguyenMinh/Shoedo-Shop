package poly.edu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.*;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

@Service
public class UserService {

    @Autowired
    private UsersDAO usersDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private QuanTriDAO quanTriDAO;

    @Autowired
    private DiaChiDAO diaChiDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== AUTH ====================

    public AuthInfo checkPermission() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return AuthInfo.failure(401, "Chưa đăng nhập");
        }

        QuanTri quanTri = quanTriDAO.findByUser_MaUser(currentUser.getMaUser());
        if (quanTri == null) {
            return AuthInfo.failure(403, "Không có quyền truy cập");
        }

        return AuthInfo.success(currentUser, quanTri, quanTri.getRole());
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AuthInfo {
        private boolean success;
        private int status;
        private String message;
        private Users user;
        private QuanTri quanTri;
        private boolean isAdmin;

        public static AuthInfo success(Users user, QuanTri quanTri, boolean isAdmin) {
            return new AuthInfo(true, 200, null, user, quanTri, isAdmin);
        }

        public static AuthInfo failure(int status, String message) {
            return new AuthInfo(false, status, message, null, null, false);
        }
    }

    // ==================== VALIDATION ====================

    public Map<String, String> validateUserData(Map<String, Object> userData, boolean isUpdate) {
        Map<String, String> errors = new HashMap<>();

        String userName = (String) userData.get("userName");
        String mail = (String) userData.get("mail");
        String password = (String) userData.get("password");
        String hoTen = (String) userData.get("hoTen");
        String sdt = (String) userData.get("sdt");
        String role = (String) userData.get("role");

        if (userName == null || userName.trim().isEmpty()) {
            errors.put("userName", "Username không được để trống");
        } else if (userName.length() < 3) {
            errors.put("userName", "Username phải có ít nhất 3 ký tự");
        } else if (!isUpdate && usersDAO.existsByUserName(userName)) {
            errors.put("userName", "Username đã tồn tại");
        }

        if (mail == null || mail.trim().isEmpty()) {
            errors.put("mail", "Email không được để trống");
        } else if (!mail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("mail", "Email không hợp lệ");
        } else if (!isUpdate && usersDAO.existsByMail(mail)) {
            errors.put("mail", "Email đã tồn tại");
        }

        if (!isUpdate && (password == null || password.trim().isEmpty())) {
            errors.put("password", "Mật khẩu không được để trống");
        } else if (!isUpdate && password.length() < 6) {
            errors.put("password", "Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (hoTen == null || hoTen.trim().isEmpty()) {
            errors.put("hoTen", "Họ tên không được để trống");
        }

        if (sdt != null && !sdt.trim().isEmpty() && !sdt.matches("^[0-9]{9,11}$")) {
            errors.put("sdt", "Số điện thoại không hợp lệ");
        }

        if (role == null || role.trim().isEmpty()) {
            errors.put("role", "Vai trò không được để trống");
        } else if (!role.matches("ADMIN|EMPLOYEE|CUSTOMER")) {
            errors.put("role", "Vai trò không hợp lệ");
        }

        return errors;
    }

    // ==================== CONVERTERS ====================

    public Map<String, Object> convertToUserMap(Users user) {
        Map<String, Object> map = new HashMap<>();
        map.put("maUser", user.getMaUser());
        map.put("userName", user.getUserName());
        map.put("mail", user.getMail());
        map.put("isActive", user.getIsActive());
        map.put("createAt", user.getCreateAt());

        if (user.getQuanTri() != null) {
            QuanTri qt = user.getQuanTri();
            // Role = true là Admin, false là Employee
            map.put("role", qt.getRole() ? "ADMIN" : "EMPLOYEE");
            map.put("hoTen", qt.getTenQT());
            map.put("sdt", "");
        } else if (user.getKhachHang() != null) {
            KhachHang kh = user.getKhachHang();
            map.put("role", "CUSTOMER");
            map.put("hoTen", kh.getTenKH());
            map.put("sdt", kh.getSdt());
        } else {
            map.put("role", "UNKNOWN");
            map.put("hoTen", "");
            map.put("sdt", "");
        }

        return map;
    }

    public Map<String, Object> convertToUserDetailMap(Users user) {
        Map<String, Object> map = new HashMap<>();
        map.put("maUser", user.getMaUser());
        map.put("userName", user.getUserName());
        map.put("mail", user.getMail());
        map.put("isActive", user.getIsActive());
        map.put("createAt", user.getCreateAt());

        if (user.getQuanTri() != null) {
            QuanTri qt = user.getQuanTri();
            map.put("role", qt.getRole() ? "ADMIN" : "EMPLOYEE");
            map.put("hoTen", qt.getTenQT());
            map.put("sdt", "");
            map.put("diaChis", Collections.emptyList());

            List<Map<String, Object>> hoaDonList = qt.getHoaDons().stream()
                    .sorted((a, b) -> b.getNgayMua().compareTo(a.getNgayMua()))
                    .limit(5)
                    .map(this::convertHoaDonToMap)
                    .collect(Collectors.toList());
            map.put("hoaDons", hoaDonList);

        } else if (user.getKhachHang() != null) {
            KhachHang kh = user.getKhachHang();
            map.put("role", "CUSTOMER");
            map.put("hoTen", kh.getTenKH());
            map.put("sdt", kh.getSdt());

            List<Map<String, Object>> diaChiList = kh.getDiaChis().stream()
                    .map(this::convertDiaChiToMap)
                    .collect(Collectors.toList());
            map.put("diaChis", diaChiList);

            List<Map<String, Object>> hoaDonList = kh.getHoaDons().stream()
                    .sorted((a, b) -> b.getNgayMua().compareTo(a.getNgayMua()))
                    .limit(5)
                    .map(this::convertHoaDonToMap)
                    .collect(Collectors.toList());
            map.put("hoaDons", hoaDonList);
        }

        return map;
    }

    public Map<String, Object> convertDiaChiToMap(DiaChi dc) {
        Map<String, Object> dcMap = new HashMap<>();
        dcMap.put("maDC", dc.getMaDC());
        dcMap.put("diemGiao", dc.getDiemGiao());
        dcMap.put("tenNN", dc.getTenNN());
        dcMap.put("sdt", dc.getSdt());
        dcMap.put("macDinh", dc.getMacDinh());
        return dcMap;
    }

    public Map<String, Object> convertHoaDonToMap(HoaDon hd) {
        Map<String, Object> map = new HashMap<>();
        map.put("maHD", hd.getMaHD());
        map.put("ngayMua", hd.getNgayMua());
        map.put("phuongThucTT", hd.getPhuongThucTT());
        map.put("trangThai", hd.getTrangThai());
        map.put("ghiChu", hd.getGhiChu());

        if (hd.getDiaChiJson() != null && !hd.getDiaChiJson().isEmpty()) {
            try {
                DiaChiJsonDTO diaChi = objectMapper.readValue(hd.getDiaChiJson(), DiaChiJsonDTO.class);
                map.put("diaChi", Map.of(
                        "tenNN", diaChi.getTenNN(),
                        "sdt", diaChi.getSdt(),
                        "diemGiao", diaChi.getDiemGiao()
                ));
            } catch (Exception e) {
                map.put("diaChi", null);
            }
        }

        return map;
    }

    // ==================== BUSINESS LOGIC ====================

    public Page<Users> getUsersByFilter(String keyword, String role, Boolean isActive, Pageable pageable, boolean isAdmin) {
        String roleFilter = "";

        // Chuyển đổi role từ frontend sang định dạng DAO hiểu
        if ("ADMIN".equals(role)) {
            roleFilter = "ADMIN"; // Filter admin
        } else if ("EMPLOYEE".equals(role)) {
            roleFilter = "EMPLOYEE"; // Filter employee
        } else if ("CUSTOMER".equals(role)) {
            roleFilter = "KH"; // KhachHang
        } else {
            roleFilter = ""; // Tất cả
        }
        Page<Users> result;
        if (isAdmin) {
            // Admin có thể xem tất cả
            if ("ADMIN".equals(roleFilter)) {
                result = usersDAO.findByAdmin(keyword, isActive, pageable);
            } else if ("EMPLOYEE".equals(roleFilter)) {
                result = usersDAO.findByEmployee(keyword, isActive, pageable);
            } else if ("KH".equals(roleFilter)) {
                result = usersDAO.findByCustomer(keyword, isActive, pageable);
            } else {
                result = usersDAO.findByFilter(keyword, "", isActive, pageable);
            }
        } else {
            // Employee chỉ xem được customer
            result = usersDAO.findByCustomer(keyword, isActive, pageable);
        }
        return result;
    }

    public Optional<Users> findById(Integer id) {
        return usersDAO.findById(id);
    }

    public boolean canAccessUser(Users targetUser, boolean isAdmin) {
        return isAdmin || targetUser.getQuanTri() == null;
    }

    @Transactional
    public Users createUser(Map<String, Object> userData, boolean isAdmin) {
        String role = (String) userData.get("role");
        String userName = (String) userData.get("userName");
        String mail = (String) userData.get("mail");

        Users newUser = new Users();
        newUser.setUserName(userName);
        newUser.setMail(mail);
        newUser.setPassWord(passwordEncoder.encode((String) userData.get("password")));
        newUser.setIsActive(true);
        newUser.setCreateAt(new Date());
        usersDAO.save(newUser);

        if ("CUSTOMER".equals(role)) {
            KhachHang kh = new KhachHang();
            kh.setUser(newUser);
            kh.setTenKH((String) userData.get("hoTen"));
            kh.setSdt((String) userData.get("sdt"));
            khachHangDAO.save(kh);
        } else if (isAdmin && ("EMPLOYEE".equals(role) || "ADMIN".equals(role))) {
            QuanTri qt = new QuanTri();
            qt.setUser(newUser);
            qt.setTenQT((String) userData.get("hoTen"));
            qt.setRole("ADMIN".equals(role));
            quanTriDAO.save(qt);
        }

        return newUser;
    }

    @Transactional
    public void updateUser(Users targetUser, Map<String, Object> userData) {
        if (userData.containsKey("isActive")) {
            targetUser.setIsActive((Boolean) userData.get("isActive"));
            usersDAO.save(targetUser);
        }

        if (targetUser.getKhachHang() != null) {
            KhachHang kh = targetUser.getKhachHang();
            if (userData.containsKey("hoTen")) kh.setTenKH((String) userData.get("hoTen"));
            if (userData.containsKey("sdt")) kh.setSdt((String) userData.get("sdt"));
            khachHangDAO.save(kh);
        } else if (targetUser.getQuanTri() != null) {
            QuanTri qt = targetUser.getQuanTri();
            if (userData.containsKey("hoTen")) qt.setTenQT((String) userData.get("hoTen"));
            quanTriDAO.save(qt);
        }
    }

    @Transactional
    public String resetPassword(Users targetUser) {
        String newPassword = generateRandomPassword();
        targetUser.setPassWord(passwordEncoder.encode(newPassword));
        usersDAO.save(targetUser);
        return newPassword;
    }

    @Transactional
    public void toggleStatus(Users targetUser) {
        targetUser.setIsActive(!targetUser.getIsActive());
        usersDAO.save(targetUser);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", usersDAO.count());
        stats.put("totalCustomers", khachHangDAO.count());
        stats.put("totalEmployees", quanTriDAO.findByRoleFalse().size());
        stats.put("totalAdmins", quanTriDAO.findByRoleTrue().size());
        stats.put("totalInactive", usersDAO.findByIsActiveFalseOrIsActiveIsNull().size());
        return stats;
    }

    // ==================== EMAIL ====================

    public void sendPasswordResetEmail(Users user, String newPassword) throws Exception {
        String to = user.getMail();
        String subject = "SHOEDO SHOP - Thông báo reset mật khẩu";

        String fullname = "";
        if (user.getKhachHang() != null) fullname = user.getKhachHang().getTenKH();
        else if (user.getQuanTri() != null) fullname = user.getQuanTri().getTenQT();
        else fullname = user.getUserName();

        String htmlContent = buildPasswordResetEmail(fullname, user.getUserName(), user.getMail(), newPassword);
        emailService.sendHtmlEmail(to, subject, htmlContent);
    }

    private String buildPasswordResetEmail(String fullname, String username, String email, String newPassword) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }"
                + ".header { background: #000; color: #fff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }"
                + ".logo { max-width: 150px; margin-bottom: 10px; }"
                + ".content { padding: 20px; background: #f9f9f9; }"
                + ".password-box { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; "
                + "text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; letter-spacing: 2px; "
                + "font-family: monospace; }"
                + ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }"
                + ".warning { background: #f8d7da; color: #721c24; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                + ".info { background: #d1ecf1; color: #0c5460; padding: 10px; border-radius: 5px; margin: 15px 0; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>ShoeDo Shop - Reset mật khẩu</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Xin chào <strong>" + fullname + "</strong>,</p>"
                + "<p>Mật khẩu của bạn đã được reset bởi quản trị viên.</p>"
                + "<div class='info'>"
                + "<p><strong>Thông tin tài khoản:</strong></p>"
                + "<p>• Username: <strong>" + username + "</strong></p>"
                + "<p>• Email: <strong>" + email + "</strong></p>"
                + "</div>"
                + "<p>Mật khẩu mới của bạn là:</p>"
                + "<div class='password-box'>"
                + newPassword
                + "</div>"
                + "<div class='warning'>"
                + "<p><strong>Lưu ý quan trọng:</strong></p>"
                + "<p>• Vui lòng đăng nhập và thay đổi mật khẩu ngay sau khi nhận được email này</p>"
                + "<p>• Không chia sẻ mật khẩu này với bất kỳ ai</p>"
                + "<p>• Nếu bạn không yêu cầu reset mật khẩu, vui lòng liên hệ với quản trị viên ngay lập tức</p>"
                + "</div>"
                + "<p>Trân trọng,<br>Đội ngũ ShoeDo Shop</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>Email này được gửi tự động từ hệ thống ShoeDo Shop.</p>"
                + "<p>© 2026 ShoeDo Shop. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
