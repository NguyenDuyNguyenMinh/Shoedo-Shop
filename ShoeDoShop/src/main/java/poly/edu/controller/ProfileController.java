package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.*;
import poly.edu.dao.*;
import poly.edu.service.AuthService;

import java.util.*;

@RestController
@RequestMapping("/api/customer")
public class ProfileController {
    
    @Autowired KhachHangDAO khachHangDAO;
    @Autowired DiaChiDAO diaChiDAO;
    @Autowired UsersDAO usersDAO;
    @Autowired AuthService authService;
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            List<DiaChi> addresses = diaChiDAO.findByKhachHang_MaKH(customer.getMaKH());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", currentUser);
            response.put("customer", customer);
            response.put("addresses", addresses);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            String fullname = request.get("fullname");
            String phone = request.get("phone");
            
            // Validate phone
            if (phone != null && !phone.matches("^[0-9]{9,11}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại không hợp lệ (9-11 số)"));
            }
            
            KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            // Check if phone already exists for another customer
            KhachHang existingCustomer = khachHangDAO.findBySdt(phone);
            if (existingCustomer != null && !existingCustomer.getMaKH().equals(customer.getMaKH())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại đã được sử dụng"));
            }
            
            customer.setTenKH(fullname);
            customer.setSdt(phone);
            
            khachHangDAO.save(customer);
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Cập nhật thông tin thành công!",
                "customer", customer
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Cập nhật thất bại: " + e.getMessage()));
        }
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            String email = authService.getCurrentUserMail();
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            String result = authService.changePassword(email, currentPassword, newPassword, confirmPassword);
            
            if (result.equals("OK")) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công!"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", result));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses() {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            List<DiaChi> addresses = diaChiDAO.findByKhachHang_MaKH(customer.getMaKH());
            addresses.sort((a, b) -> Boolean.compare(b.getMacDinh(), a.getMacDinh()));
            
            return ResponseEntity.ok(Map.of("success", true, "addresses", addresses));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }
    
    @PostMapping("/address")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, Object> request) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng"));
            }
            
            String tenNN = (String) request.get("tenNN");
            String sdt = (String) request.get("sdt");
            String diemGiao = (String) request.get("diemGiao");
            Boolean macDinh = (Boolean) request.get("macDinh");
            
            // Validate phone
            if (sdt != null && !sdt.matches("^[0-9]{9,11}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại không hợp lệ (9-11 số)"));
            }
            
            if (macDinh != null && macDinh) {
                diaChiDAO.clearDefaultAddress(customer.getMaKH());
            }
            
            DiaChi newAddress = new DiaChi();
            newAddress.setKhachHang(customer);
            newAddress.setTenNN(tenNN);
            newAddress.setSdt(sdt);
            newAddress.setDiemGiao(diemGiao);
            newAddress.setMacDinh(macDinh != null ? macDinh : false);
            
            diaChiDAO.save(newAddress);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm địa chỉ thành công!"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Thêm địa chỉ thất bại: " + e.getMessage()));
        }
    }
    
    @PutMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            Optional<DiaChi> optionalAddress = diaChiDAO.findById(id);
            if (!optionalAddress.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy địa chỉ"));
            }
            
            DiaChi address = optionalAddress.get();
            
            String tenNN = (String) request.get("tenNN");
            String sdt = (String) request.get("sdt");
            String diemGiao = (String) request.get("diemGiao");
            Boolean macDinh = (Boolean) request.get("macDinh");
            
            // Validate phone
            if (sdt != null && !sdt.matches("^[0-9]{9,11}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại không hợp lệ (9-11 số)"));
            }
            
            if (macDinh != null && macDinh && !address.getMacDinh()) {
                diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
            }
            
            address.setTenNN(tenNN);
            address.setSdt(sdt);
            address.setDiemGiao(diemGiao);
            address.setMacDinh(macDinh != null ? macDinh : address.getMacDinh());
            
            diaChiDAO.save(address);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật địa chỉ thành công!"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Cập nhật địa chỉ thất bại: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Integer id) {
        try {
            Optional<DiaChi> optionalAddress = diaChiDAO.findById(id);
            if (!optionalAddress.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy địa chỉ"));
            }
            
            DiaChi address = optionalAddress.get();
            if (address.getMacDinh()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể xóa địa chỉ mặc định"));
            }
            
            diaChiDAO.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa địa chỉ thành công!"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Xóa địa chỉ thất bại: " + e.getMessage()));
        }
    }
    
    @PostMapping("/address/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Integer id) {
        try {
            Optional<DiaChi> optionalAddress = diaChiDAO.findById(id);
            if (!optionalAddress.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy địa chỉ"));
            }
            
            DiaChi address = optionalAddress.get();
            diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
            
            address.setMacDinh(true);
            diaChiDAO.save(address);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Đặt địa chỉ mặc định thành công!"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}