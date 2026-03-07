package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Users;
import poly.edu.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/users")
public class QLUsersController {

    @Autowired
    private UserService userService;

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

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Users> userPage = userService.getUsersByFilter(keyword, role, isActive, pageable, auth.isAdmin());

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
        if (!auth.isAdmin() && !"CUSTOMER".equals(role)) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Chỉ được tạo tài khoản khách hàng"));
        }

        Map<String, String> errors = userService.validateUserData(userData, false);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "errors", errors));
        }

        userService.createUser(userData, auth.isAdmin());
        return ResponseEntity.ok(Map.of("success", true, "message", "Tạo user thành công"));
    }

    @PutMapping("/{id}")
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

        if (!userService.canAccessUser(targetUser, auth.isAdmin())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Không có quyền sửa user này"));
        }

        Map<String, String> errors = userService.validateUserData(userData, true);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "errors", errors));
        }

        userService.updateUser(targetUser, userData);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công"));
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