package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.*;
import poly.edu.dao.*;
import poly.edu.service.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/users")
public class QLUsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private UsersDAO usersDAO;

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private QuanTriDAO quanTriDAO;

    @Autowired
    private DiaChiDAO diaChiDAO;

    @Autowired
    private GioHangDAO gioHangDAO;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String role,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "maUser") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {

        UserService.AuthInfo auth = userService.checkPermission();
        if (!auth.isSuccess()) {
            return ResponseEntity.status(auth.getStatus())
                    .body(Map.of("success", false, "message", auth.getMessage()));
        }

        Boolean isActive = null;
        if ("active".equalsIgnoreCase(status)) isActive = true;
        else if ("inactive".equalsIgnoreCase(status)) isActive = false;

        String roleFilter = "";
        if ("ADMIN".equals(role)) {
            roleFilter = "ADMIN";
        } else if ("EMPLOYEE".equals(role)) {
            roleFilter = "EMPLOYEE";
        } else if ("CUSTOMER".equals(role)) {
            roleFilter = "KH";
        }

        Page<Users> userPage;

        // Xử lý riêng cho trường hợp sort theo hoTen
        if ("hoTen".equals(sortBy)) {
            Page<Object[]> objectPage = null;

            // Tạo Sort direction cho sortName
            Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by("sortName").ascending() :
                    Sort.by("sortName").descending();
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            if (auth.isAdmin()) {
                if ("ADMIN".equals(roleFilter)) {
                    objectPage = usersDAO.findByAdminOrderByHoTen(keyword, isActive, pageable);
                } else if ("EMPLOYEE".equals(roleFilter)) {
                    objectPage = usersDAO.findByEmployeeOrderByHoTen(keyword, isActive, pageable);
                } else if ("KH".equals(roleFilter)) {
                    objectPage = usersDAO.findByCustomerOrderByHoTen(keyword, isActive, pageable);
                } else {
                    objectPage = usersDAO.findByFilterOrderByHoTen(keyword, "", isActive, pageable);
                }
            } else {
                // Employee chỉ xem customer
                objectPage = usersDAO.findByCustomerOrderByHoTen(keyword, isActive, pageable);
            }

            // Chuyển đổi từ Object[] sang Users (lấy phần tử đầu tiên là Users)
            List<Users> usersList = objectPage.getContent().stream()
                    .map(obj -> (Users) obj[0])
                    .collect(Collectors.toList());

            // Tạo Page<Users> mới
            userPage = new PageImpl<>(usersList, objectPage.getPageable(), objectPage.getTotalElements());

        } else {
            // Xử lý bình thường - tạo Sort object cho các trường khác
            Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            userPage = userService.getUsersByFilter(keyword, role, isActive, pageable, auth.isAdmin());
        }

        List<Map<String, Object>> userList = userPage.getContent().stream()
                .map(userService::convertToUserMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", userList);
        response.put("stats", userService.getStats());
        response.put("currentPage", page);
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalItems", userPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable("id") Integer id) {
        UserService.AuthInfo auth = userService.checkPermission();
        if (!auth.isSuccess()) {
            return ResponseEntity.status(auth.getStatus())
                    .body(Map.of("success", false, "message", auth.getMessage()));
        }

        Users targetUser = userService.findById(id).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy user"));
        }

        if (!userService.canAccessUser(targetUser, auth.isAdmin())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Không có quyền xem user này"));
        }

        return ResponseEntity.ok(Map.of("success", true, "data", userService.convertToUserDetailMap(targetUser)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        UserService.AuthInfo auth = userService.checkPermission();
        if (!auth.isSuccess()) {
            return ResponseEntity.status(auth.getStatus())
                    .body(Map.of("success", false, "message", auth.getMessage()));
        }

        String role = (String) userData.get("role");

        // Employee chỉ được tạo CUSTOMER và EMPLOYEE (không được tạo ADMIN)
        if (!auth.isAdmin() && "ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bạn không có quyền tạo tài khoản Admin"));
        }

        Map<String, String> errors = userService.validateUserData(userData, false);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", errors,
                    "message", "Dữ liệu không hợp lệ"
            ));
        }

        try {
            userService.createUser(userData, auth.isAdmin());
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo user thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "error", e.toString()
            ));
        }
    }

    private String getCurrentRole(Users user) {
        if (user.getQuanTri() != null) {
            return user.getQuanTri().getRole() ? "ADMIN" : "EMPLOYEE";
        } else if (user.getKhachHang() != null) {
            return "CUSTOMER";
        }
        return "UNKNOWN";
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> userData) {

        UserService.AuthInfo auth = userService.checkPermission();
        if (!auth.isSuccess()) {
            return ResponseEntity.status(auth.getStatus())
                    .body(Map.of("success", false, "message", auth.getMessage()));
        }

        Users targetUser = userService.findById(id).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy user"));
        }

        // ========== KIỂM TRA QUYỀN CHI TIẾT ==========

        // 1. Employee chỉ được sửa CUSTOMER
        if (!auth.isAdmin() && targetUser.getKhachHang() == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bạn chỉ được sửa tài khoản khách hàng"));
        }

        // 2. Employee không được sửa admin khác
        if (!auth.isAdmin() && targetUser.getQuanTri() != null) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bạn không có quyền sửa tài khoản nhân viên hoặc admin"));
        }

        // 3. Admin không được sửa admin khác (chỉ được sửa chính mình và nhân viên)
        if (auth.isAdmin() && targetUser.getQuanTri() != null &&
                targetUser.getQuanTri().getRole() && // target là admin
                !targetUser.getMaUser().equals(auth.getUser().getMaUser())) { // không phải chính mình
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bạn chỉ được sửa thông tin của chính mình và nhân viên"));
        }

        // 4. Kiểm tra nếu đang cố gắng chuyển role của CUSTOMER
        if (targetUser.getKhachHang() != null && userData.containsKey("role") &&
                !"CUSTOMER".equals(userData.get("role"))) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Không thể chuyển đổi role của khách hàng"));
        }

        try {
            // Cập nhật username nếu có
            if (userData.containsKey("userName")) {
                String newUserName = (String) userData.get("userName");
                if (!targetUser.getUserName().equals(newUserName)) {
                    if (usersDAO.existsByUserName(newUserName)) {
                        throw new RuntimeException("Username đã tồn tại");
                    }
                    targetUser.setUserName(newUserName);
                }
            }

            // Cập nhật email nếu có
            if (userData.containsKey("mail")) {
                String newMail = (String) userData.get("mail");
                if (!targetUser.getMail().equals(newMail)) {
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
                    // Admin có thể chuyển role kể cả khi có đơn hàng
                    convertUserRoleWithData(targetUser, newRole, userData);
                } else {
                    // Role không thay đổi, chỉ cập nhật thông tin cơ bản
                    updateUserInfo(targetUser, userData);
                }
            } else {
                // Không thay đổi role, chỉ cập nhật thông tin
                updateUserInfo(targetUser, userData);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật thành công"
            ));

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", e.toString());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Có lỗi xảy ra khi cập nhật user");
            errorResponse.put("error", e.toString());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Phương thức chuyển đổi role đơn giản (khi chưa có đơn hàng)
    private void simpleRoleChange(Users targetUser, String newRole, Map<String, Object> userData) {
        // Xóa dữ liệu cũ
        if (targetUser.getKhachHang() != null) {
            KhachHang kh = targetUser.getKhachHang();

            // Xóa giỏ hàng trước
            if (kh.getGioHangs() != null && !kh.getGioHangs().isEmpty()) {
                for (GioHang gh : kh.getGioHangs()) {
                    gioHangDAO.delete(gh);
                }
            }

            // Xóa địa chỉ trước
            if (kh.getDiaChis() != null && !kh.getDiaChis().isEmpty()) {
                for (DiaChi dc : kh.getDiaChis()) {
                    diaChiDAO.delete(dc);
                }
            }

            khachHangDAO.delete(kh);
            targetUser.setKhachHang(null);
        }
        if (targetUser.getQuanTri() != null) {
            quanTriDAO.delete(targetUser.getQuanTri());
            targetUser.setQuanTri(null);
        }

        // Tạo dữ liệu mới
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
    }

    // Phương thức chuyển đổi role khi có đơn hàng (giữ lại đơn hàng)
    private void convertUserRoleWithData(Users targetUser, String newRole, Map<String, Object> userData) {
        // Lưu lại thông tin cần chuyển đổi
        String hoTen = (String) userData.get("hoTen");
        String sdt = (String) userData.get("sdt");

        // Lưu lại lịch sử đơn hàng
        List<HoaDon> hoaDons = new ArrayList<>();

        if (targetUser.getKhachHang() != null) {
            hoaDons = new ArrayList<>(targetUser.getKhachHang().getHoaDons());
        } else if (targetUser.getQuanTri() != null) {
            hoaDons = new ArrayList<>(targetUser.getQuanTri().getHoaDons());
        }

        // Xóa dữ liệu cũ
        if (targetUser.getKhachHang() != null) {
            KhachHang kh = targetUser.getKhachHang();

            // Cập nhật các hóa đơn để không bị mất tham chiếu
            for (HoaDon hd : hoaDons) {
                hd.setKhachHang(null);
                hoaDonDAO.save(hd);
            }

            // Xóa giỏ hàng trước
            if (kh.getGioHangs() != null && !kh.getGioHangs().isEmpty()) {
                for (GioHang gh : kh.getGioHangs()) {
                    gioHangDAO.delete(gh);
                }
            }

            // Xóa địa chỉ trước
            if (kh.getDiaChis() != null && !kh.getDiaChis().isEmpty()) {
                for (DiaChi dc : kh.getDiaChis()) {
                    diaChiDAO.delete(dc);
                }
            }

            khachHangDAO.delete(kh);
            targetUser.setKhachHang(null);
        }

        if (targetUser.getQuanTri() != null) {
            QuanTri qt = targetUser.getQuanTri();

            // Cập nhật các hóa đơn để không bị mất tham chiếu
            for (HoaDon hd : hoaDons) {
                hd.setQuanTri(null);
                hoaDonDAO.save(hd);
            }

            quanTriDAO.delete(qt);
            targetUser.setQuanTri(null);
        }

        // Tạo dữ liệu mới theo role mới
        if ("CUSTOMER".equals(newRole)) {
            KhachHang kh = new KhachHang();
            kh.setUser(targetUser);
            kh.setTenKH(hoTen);
            kh.setSdt(sdt);
            khachHangDAO.save(kh);

            // Gán lại đơn hàng cũ
            for (HoaDon hd : hoaDons) {
                hd.setKhachHang(kh);
                hoaDonDAO.save(hd);
            }
        } else if ("EMPLOYEE".equals(newRole) || "ADMIN".equals(newRole)) {
            QuanTri qt = new QuanTri();
            qt.setUser(targetUser);
            qt.setTenQT(hoTen);
            qt.setRole("ADMIN".equals(newRole));
            quanTriDAO.save(qt);

            // Gán lại đơn hàng cũ
            for (HoaDon hd : hoaDons) {
                hd.setQuanTri(qt);
                hoaDonDAO.save(hd);
            }
        }
    }

    // Cập nhật thông tin khi role không thay đổi
    private void updateUserInfo(Users targetUser, Map<String, Object> userData) {
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

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable("id") Integer id) {
        UserService.AuthInfo auth = userService.checkPermission();
        if (!auth.isSuccess()) {
            return ResponseEntity.status(auth.getStatus())
                    .body(Map.of("success", false, "message", auth.getMessage()));
        }

        Users targetUser = userService.findById(id).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy user"));
        }

        if (!userService.canAccessUser(targetUser, auth.isAdmin())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Không có quyền reset password user này"));
        }

        String newPassword = userService.resetPassword(targetUser);

        try {
            userService.sendPasswordResetEmail(targetUser, newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reset mật khẩu thành công. Email đã được gửi đến người dùng.",
                "newPassword", newPassword
        ));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable("id") Integer id) {
        UserService.AuthInfo auth = userService.checkPermission();
        if (!auth.isSuccess()) {
            return ResponseEntity.status(auth.getStatus())
                    .body(Map.of("success", false, "message", auth.getMessage()));
        }

        Users targetUser = userService.findById(id).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Không tìm thấy user"));
        }

        if (!userService.canAccessUser(targetUser, auth.isAdmin())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Không có quyền thay đổi trạng thái user này"));
        }

        try {
            userService.toggleStatus(targetUser);
            String status = targetUser.getIsActive() ? "mở khóa" : "khóa";
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã " + status + " tài khoản"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}