package poly.edu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.*;
import poly.edu.dto.*;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UsersDAO usersDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private QuanTriDAO quanTriDAO;

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private DiaChiDAO diaChiDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailAsyncService emailAsyncService;

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

    @Transactional
    public void convertUserRole(Users targetUser, String newRole) {
        String currentRole = getCurrentRole(targetUser);

        if (currentRole.equals(newRole)) {
            throw new RuntimeException("Người dùng đã ở vai trò này");
        }

        // Kiểm tra quyền
        AuthInfo auth = checkPermission();
        if (!auth.isSuccess() || !auth.isAdmin()) {
            throw new RuntimeException("Chỉ admin mới có quyền chuyển đổi vai trò");
        }

        // Lưu lại thông tin cần chuyển đổi
        String hoTen = "";
        String sdt = "";
        Date createAt = targetUser.getCreateAt();
        Boolean isActive = targetUser.getIsActive();

        // Lưu lại lịch sử đơn hàng
        List<HoaDon> hoaDons = new ArrayList<>();

        if (targetUser.getKhachHang() != null) {
            KhachHang kh = targetUser.getKhachHang();
            hoTen = kh.getTenKH();
            sdt = kh.getSdt() != null ? kh.getSdt() : "";
            hoaDons = new ArrayList<>(kh.getHoaDons()); // Tạo bản sao để tránh lỗi khi xóa
        } else if (targetUser.getQuanTri() != null) {
            QuanTri qt = targetUser.getQuanTri();
            hoTen = qt.getTenQT();
            sdt = ""; // Quản trị không có SĐT
            hoaDons = new ArrayList<>(qt.getHoaDons()); // Tạo bản sao để tránh lỗi khi xóa
        }

        // Xóa dữ liệu cũ
        if (targetUser.getKhachHang() != null) {
            // Cập nhật các hóa đơn để không bị mất tham chiếu
            for (HoaDon hd : hoaDons) {
                hd.setKhachHang(null);
                hoaDonDAO.save(hd);
            }
            khachHangDAO.delete(targetUser.getKhachHang());
            targetUser.setKhachHang(null);
        }

        if (targetUser.getQuanTri() != null) {
            // Cập nhật các hóa đơn đã xử lý
            for (HoaDon hd : hoaDons) {
                hd.setQuanTri(null);
                hoaDonDAO.save(hd);
            }
            quanTriDAO.delete(targetUser.getQuanTri());
            targetUser.setQuanTri(null);
        }

        // Tạo dữ liệu mới theo role mới
        if ("CUSTOMER".equals(newRole)) {
            // Chuyển thành khách hàng
            KhachHang kh = new KhachHang();
            kh.setUser(targetUser);
            kh.setTenKH(hoTen);
            kh.setSdt(sdt);
            khachHangDAO.save(kh);

            // Gán lại đơn hàng cũ (nếu có) cho khách hàng mới
            for (HoaDon hd : hoaDons) {
                hd.setKhachHang(kh);
                hoaDonDAO.save(hd);
            }

        } else if ("EMPLOYEE".equals(newRole) || "ADMIN".equals(newRole)) {
            // Chuyển thành nhân viên/admin
            QuanTri qt = new QuanTri();
            qt.setUser(targetUser);
            qt.setTenQT(hoTen);
            qt.setRole("ADMIN".equals(newRole));
            quanTriDAO.save(qt);

            // Gán lại đơn hàng đã xử lý cho nhân viên mới
            for (HoaDon hd : hoaDons) {
                hd.setQuanTri(qt);
                hoaDonDAO.save(hd);
            }
        }

        // Cập nhật thông tin user
        targetUser.setCreateAt(createAt);
        targetUser.setIsActive(isActive);
        usersDAO.save(targetUser);
    }

    // ==================== BUSINESS LOGIC ====================

    public Page<Users> getUsersByFilter(String keyword, String role, Boolean isActive, Pageable pageable, boolean isAdmin) {
        String roleFilter = "";

        // Chuyển đổi role từ frontend sang định dạng DAO hiểu
        if ("ADMIN".equals(role)) {
            roleFilter = "ADMIN";
        } else if ("EMPLOYEE".equals(role)) {
            roleFilter = "EMPLOYEE";
        } else if ("CUSTOMER".equals(role)) {  // Sửa từ "KH" thành "CUSTOMER"
            roleFilter = "KH";
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
        String password = (String) userData.get("password");
        String hoTen = (String) userData.get("hoTen");
        String sdt = (String) userData.get("sdt");

        // Kiểm tra username đã tồn tại
        if (usersDAO.existsByUserName(userName)) {
            throw new RuntimeException("Username đã tồn tại trong hệ thống");
        }

        // Kiểm tra email đã tồn tại
        if (usersDAO.existsByMail(mail)) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        // Kiểm tra mật khẩu
        if (password == null || password.length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        Users newUser = new Users();
        newUser.setUserName(userName);
        newUser.setMail(mail);
        newUser.setPassWord(passwordEncoder.encode(password));
        newUser.setIsActive(true);
        newUser.setCreateAt(new Date());
        usersDAO.save(newUser);

        if ("CUSTOMER".equals(role)) {
            KhachHang kh = new KhachHang();
            kh.setUser(newUser);
            kh.setTenKH(hoTen);
            kh.setSdt(sdt);
            khachHangDAO.save(kh);
        } else if ("EMPLOYEE".equals(role) || "ADMIN".equals(role)) {
            // Employee chỉ được tạo EMPLOYEE, không được tạo ADMIN
            if (!isAdmin && "ADMIN".equals(role)) {
                throw new RuntimeException("Bạn không có quyền tạo tài khoản Admin");
            }

            QuanTri qt = new QuanTri();
            qt.setUser(newUser);
            qt.setTenQT(hoTen);
            qt.setRole("ADMIN".equals(role));
            quanTriDAO.save(qt);
        }

        return newUser;
    }

    @Transactional
    public void updateUser(Users targetUser, Map<String, Object> userData) {
        // Cập nhật username nếu có và user hiện tại là admin
        if (userData.containsKey("userName")) {
            String newUserName = (String) userData.get("userName");
            if (!targetUser.getUserName().equals(newUserName)) {
                // Kiểm tra username mới đã tồn tại chưa
                if (usersDAO.existsByUserName(newUserName)) {
                    throw new RuntimeException("Username đã tồn tại");
                }
                targetUser.setUserName(newUserName);
            }
        }

        // Cập nhật email nếu có và user hiện tại là admin
        if (userData.containsKey("mail")) {
            String newMail = (String) userData.get("mail");
            if (!targetUser.getMail().equals(newMail)) {
                // Kiểm tra email mới đã tồn tại chưa
                if (usersDAO.existsByMail(newMail)) {
                    throw new RuntimeException("Email đã tồn tại");
                }
                targetUser.setMail(newMail);
            }
        }

        // Cập nhật trạng thái active
        if (userData.containsKey("isActive")) {
            targetUser.setIsActive((Boolean) userData.get("isActive"));
        }

        usersDAO.save(targetUser);

        // Xử lý thay đổi role
        if (userData.containsKey("role")) {
            String newRole = (String) userData.get("role");
            String currentRole = getCurrentRole(targetUser);

            // Nếu role thay đổi
            if (!currentRole.equals(newRole)) {
                // Kiểm tra xem user có hóa đơn không
                boolean hasOrders = false;
                if (targetUser.getQuanTri() != null) {
                    // Kiểm tra nhân viên đã xử lý đơn hàng chưa
                    hasOrders = !targetUser.getQuanTri().getHoaDons().isEmpty();
                    if (hasOrders) {
                        throw new RuntimeException("Không thể thay đổi vai trò của nhân viên đã xử lý đơn hàng");
                    }
                } else if (targetUser.getKhachHang() != null) {
                    // Kiểm tra khách hàng đã có đơn hàng chưa
                    hasOrders = !targetUser.getKhachHang().getHoaDons().isEmpty();
                    if (hasOrders) {
                        throw new RuntimeException("Không thể thay đổi vai trò của khách hàng đã có đơn hàng");
                    }
                }

                // Xóa dữ liệu cũ
                if (targetUser.getKhachHang() != null) {
                    khachHangDAO.delete(targetUser.getKhachHang());
                    targetUser.setKhachHang(null);
                }
                if (targetUser.getQuanTri() != null) {
                    quanTriDAO.delete(targetUser.getQuanTri());
                    targetUser.setQuanTri(null);
                }

                // Tạo dữ liệu mới theo role mới
                if ("CUSTOMER".equals(newRole)) {
                    KhachHang kh = new KhachHang();
                    kh.setUser(targetUser);
                    kh.setTenKH((String) userData.get("hoTen"));
                    kh.setSdt((String) userData.get("sdt"));
                    khachHangDAO.save(kh);
                } else if ("EMPLOYEE".equals(newRole) || "ADMIN".equals(newRole)) {
                    QuanTri qt = new QuanTri();
                    qt.setUser(targetUser);
                    qt.setTenQT((String) userData.get("hoTen"));
                    qt.setRole("ADMIN".equals(newRole));
                    quanTriDAO.save(qt);
                }
            } else {
                // Role không thay đổi, chỉ cập nhật thông tin cơ bản
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
        } else {
            // Không thay đổi role, chỉ cập nhật thông tin
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
    }

    // Thêm phương thức helper
    private String getCurrentRole(Users user) {
        if (user.getQuanTri() != null) {
            return user.getQuanTri().getRole() ? "ADMIN" : "EMPLOYEE";
        } else if (user.getKhachHang() != null) {
            return "CUSTOMER";
        }
        return "UNKNOWN";
    }

    @Transactional
    public String resetPassword(Users targetUser) {
        String newPassword = generateRandomPassword();
        targetUser.setPassWord(passwordEncoder.encode(newPassword));
        usersDAO.save(targetUser);

        // Gửi email ngay lập tức bằng Async
        try {
            sendPasswordResetEmailAsync(targetUser, newPassword);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email reset mật khẩu: " + e.getMessage());
            e.printStackTrace();
            // Không throw exception vì mật khẩu đã được lưu thành công
        }

        return newPassword;
    }

    /**
     * Gửi email reset mật khẩu bất đồng bộ (không chặn luồng chính)
     */
    public void sendPasswordResetEmailAsync(Users user, String newPassword) {
        emailAsyncService.sendPasswordResetByAdminEmail(
                user.getMail(),
                getUserFullname(user),
                user.getUserName(),
                newPassword
        );
    }

    /**
     * Lấy tên đầy đủ của user
     */
    private String getUserFullname(Users user) {
        if (user.getKhachHang() != null) {
            return user.getKhachHang().getTenKH();
        } else if (user.getQuanTri() != null) {
            return user.getQuanTri().getTenQT();
        }
        return user.getUserName();
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

        String fullname = getUserFullname(user);

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