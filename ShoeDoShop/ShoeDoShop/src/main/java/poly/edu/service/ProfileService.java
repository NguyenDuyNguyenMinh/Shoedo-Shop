package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.*;
import poly.edu.entity.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProfileService {
    
    @Autowired private KhachHangDAO khachHangDAO;
    @Autowired private DiaChiDAO diaChiDAO;
    @Autowired private UsersDAO usersDAO;
    @Autowired private AuthService authService;
    
    public Map<String, Object> getProfile() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        List<DiaChi> addresses = diaChiDAO.findByKhachHang_MaKH(customer.getMaKH());
        
        String createAt = "";
        if (currentUser.getCreateAt() != null) {
            createAt = new SimpleDateFormat("dd/MM/yyyy").format(currentUser.getCreateAt());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("maUser", currentUser.getMaUser());
        userInfo.put("userName", currentUser.getUserName() != null ? currentUser.getUserName().trim() : "");
        userInfo.put("mail", currentUser.getMail());
        userInfo.put("createAt", createAt);
        userInfo.put("isActive", currentUser.getIsActive());
        
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("maKH", customer.getMaKH());
        customerInfo.put("tenKH", customer.getTenKH());
        customerInfo.put("sdt", customer.getSdt());
        
        response.put("user", userInfo);
        response.put("customer", customerInfo);
        response.put("addresses", addresses);
        
        return response;
    }
    
    public Map<String, Object> updateProfile(Map<String, String> request) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        String userName = request.get("userName");
        String fullname = request.get("fullname");
        String phone = request.get("phone");

        if (userName != null) {
            userName = userName.trim();
            if (userName.isEmpty()) {
                return Map.of("success", false, "message", "Tên đăng nhập không được để trống");
            }
            if (userName.length() < 3) {
                return Map.of("success", false, "message", "Tên đăng nhập phải có ít nhất 3 ký tự");
            }
            
            Users existingUser = usersDAO.findByUserName(userName);
            if (existingUser != null && !existingUser.getMaUser().equals(currentUser.getMaUser())) {
                return Map.of("success", false, "message", "Tên đăng nhập đã được sử dụng");
            }
            currentUser.setUserName(userName);
            usersDAO.save(currentUser);
        }

        if (fullname == null || fullname.trim().isEmpty()) {
            return Map.of("success", false, "message", "Họ và tên không được để trống");
        }
        fullname = fullname.trim();
        
        if (phone == null || phone.trim().isEmpty()) {
            return Map.of("success", false, "message", "Số điện thoại không được để trống");
        }
        phone = phone.trim();
        if (!phone.matches("^[0-9]{9,11}$")) {
            return Map.of("success", false, "message", "Số điện thoại không hợp lệ");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        KhachHang existingCustomer = khachHangDAO.findBySdt(phone);
        if (existingCustomer != null && !existingCustomer.getMaKH().equals(customer.getMaKH())) {
            return Map.of("success", false, "message", "Số điện thoại đã được sử dụng");
        }
        
        customer.setTenKH(fullname);
        customer.setSdt(phone);
        
        khachHangDAO.save(customer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật thông tin thành công!");
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userName", currentUser.getUserName());
        userInfo.put("mail", currentUser.getMail());
        
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("tenKH", customer.getTenKH());
        customerInfo.put("sdt", customer.getSdt());
        
        response.put("user", userInfo);
        response.put("customer", customerInfo);
        
        return response;
    }
    
    public Map<String, Object> changePassword(Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");
        
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        String result = authService.changePassword(
            currentUser.getMail(), currentPassword, newPassword, confirmPassword
        );
        
        if (result.equals("OK")) {
            return Map.of("success", true, "message", "Đổi mật khẩu thành công!");
        } else {
            return Map.of("success", false, "message", result);
        }
    }
    
    public Map<String, Object> getAddresses() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        List<DiaChi> addresses = diaChiDAO.findByKhachHang_MaKH(customer.getMaKH());
        addresses.sort((a, b) -> Boolean.compare(b.getMacDinh(), a.getMacDinh()));
        
        return Map.of("success", true, "addresses", addresses);
    }
    
    public Map<String, Object> addAddress(Map<String, Object> request) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        String tenNN = ((String) request.get("tenNN")).trim();
        String sdt = ((String) request.get("sdt")).trim();
        String diemGiao = ((String) request.get("diemGiao")).trim();
        Boolean macDinh = request.get("macDinh") != null ? (Boolean) request.get("macDinh") : false;
        
        // Validate
        if (tenNN.isEmpty()) return Map.of("success", false, "message", "Vui lòng nhập tên người nhận");
        if (sdt.isEmpty()) return Map.of("success", false, "message", "Vui lòng nhập số điện thoại");
        if (!sdt.matches("^[0-9]{9,11}$")) return Map.of("success", false, "message", "Số điện thoại không hợp lệ");
        if (diemGiao.isEmpty()) return Map.of("success", false, "message", "Vui lòng nhập địa chỉ");
        
        if (macDinh) diaChiDAO.clearDefaultAddress(customer.getMaKH());
        
        DiaChi newAddress = new DiaChi();
        newAddress.setKhachHang(customer);
        newAddress.setTenNN(tenNN);
        newAddress.setSdt(sdt);
        newAddress.setDiemGiao(diemGiao);
        newAddress.setMacDinh(macDinh);
        
        diaChiDAO.save(newAddress);
        
        return Map.of("success", true, "message", "Thêm địa chỉ thành công!");
    }
    
    public Map<String, Object> updateAddress(Integer id, Map<String, Object> request) {
        Optional<DiaChi> optionalAddress = diaChiDAO.findById(id);
        if (!optionalAddress.isPresent()) {
            return Map.of("success", false, "message", "Không tìm thấy địa chỉ");
        }
        
        DiaChi address = optionalAddress.get();
        
        String tenNN = ((String) request.get("tenNN")).trim();
        String sdt = ((String) request.get("sdt")).trim();
        String diemGiao = ((String) request.get("diemGiao")).trim();
        Boolean macDinh = request.get("macDinh") != null ? (Boolean) request.get("macDinh") : address.getMacDinh();
        
        // Validate
        if (tenNN.isEmpty()) return Map.of("success", false, "message", "Vui lòng nhập tên người nhận");
        if (sdt.isEmpty()) return Map.of("success", false, "message", "Vui lòng nhập số điện thoại");
        if (!sdt.matches("^[0-9]{9,11}$")) return Map.of("success", false, "message", "Số điện thoại không hợp lệ");
        if (diemGiao.isEmpty()) return Map.of("success", false, "message", "Vui lòng nhập địa chỉ");
        
        if (macDinh && !address.getMacDinh()) {
            diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
        }
        
        address.setTenNN(tenNN);
        address.setSdt(sdt);
        address.setDiemGiao(diemGiao);
        address.setMacDinh(macDinh);
        
        diaChiDAO.save(address);
        
        return Map.of("success", true, "message", "Cập nhật địa chỉ thành công!");
    }
    
    public Map<String, Object> deleteAddress(Integer id) {
        Optional<DiaChi> optionalAddress = diaChiDAO.findById(id);
        if (!optionalAddress.isPresent()) {
            return Map.of("success", false, "message", "Không tìm thấy địa chỉ");
        }
        
        DiaChi address = optionalAddress.get();
        if (address.getMacDinh()) {
            return Map.of("success", false, "message", "Không thể xóa địa chỉ mặc định");
        }
        
        diaChiDAO.deleteById(id);
        return Map.of("success", true, "message", "Xóa địa chỉ thành công!");
    }
    
    public Map<String, Object> setDefaultAddress(Integer id) {
        Optional<DiaChi> optionalAddress = diaChiDAO.findById(id);
        if (!optionalAddress.isPresent()) {
            return Map.of("success", false, "message", "Không tìm thấy địa chỉ");
        }
        
        DiaChi address = optionalAddress.get();
        diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
        
        address.setMacDinh(true);
        diaChiDAO.save(address);
        
        return Map.of("success", true, "message", "Đặt địa chỉ mặc định thành công!");
    }
}