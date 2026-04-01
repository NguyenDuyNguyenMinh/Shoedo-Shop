package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.dao.*;
import poly.edu.entity.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    
    @Autowired private KhachHangDAO khachHangDAO;
    @Autowired private DiaChiDAO diaChiDAO;
    @Autowired private UsersDAO usersDAO;
    @Autowired private AuthService authService;
    @Autowired private KhachHangVoucherDAO khachHangVoucherDAO;
    @Autowired private VoucherDAO voucherDAO; 
    @Autowired private LichSuTichDiemDAO lichSuTichDiemDAO;
    
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("maUser", currentUser.getMaUser());
        userInfo.put("userName", currentUser.getUserName() != null ? currentUser.getUserName().trim() : "");
        userInfo.put("mail", currentUser.getMail());
        userInfo.put("isActive", currentUser.getIsActive());
        
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("maKH", customer.getMaKH());
        customerInfo.put("tenKH", customer.getTenKH());
        customerInfo.put("sdt", customer.getSdt());
        customerInfo.put("diemTichLuy", customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0);
        customerInfo.put("maGioiThieu", customer.getMaGioiThieu() != null ? customer.getMaGioiThieu() : "");
        customerInfo.put("hasAppliedReferral", customer.getMaNguoiGioiThieu() != null && !customer.getMaNguoiGioiThieu().isEmpty());
        
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
    
    public Map<String, Object> getPointsHistory() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        List<LichSuTichDiem> history = lichSuTichDiemDAO.findByKhachHangOrderByNgayGiaoDichDesc(customer);
        
        List<Map<String, Object>> formattedHistory = history.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("soDiem", item.getSoDiem());
            map.put("loaiGiaoDich", item.getLoaiGiaoDich());
            map.put("ngayGiaoDich", item.getNgayGiaoDich());
            if (item.getNguoiLienQuan() != null) {
                map.put("nguoiLienQuan", item.getNguoiLienQuan().getTenKH());
            }
            return map;
        }).collect(Collectors.toList());
        
        return Map.of("success", true, "history", formattedHistory, "currentPoints", customer.getDiemTichLuy());
    }
    
    public Map<String, Object> getMyVouchers() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        List<KhachHangVoucher> myVouchers = khachHangVoucherDAO.findByKhachHang(customer);
        
        List<Map<String, Object>> formattedVouchers = myVouchers.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("maKHVC", item.getMaKHVC());
            map.put("voucher", item.getVoucher());
            map.put("trangThai", item.getTrangThai());
            map.put("ngayDoi", item.getNgayDoi());
            map.put("hanSuDung", item.getHanSuDung());
            return map;
        }).collect(Collectors.toList());
        
        return Map.of("success", true, "vouchers", formattedVouchers);
    }
    
    public Map<String, Object> getAvailableVouchers() {
        try {
            Date now = new Date();
            List<Voucher> availableVouchers = new ArrayList<>();
            
            try {
                availableVouchers = voucherDAO.findAvailableVouchers(now);
            } catch (Exception e) {
                availableVouchers = voucherDAO.findByIsActiveTrueAndSoLuongGreaterThan(0);
            }
            
            List<Voucher> filteredVouchers = new ArrayList<>();
            for (Voucher v : availableVouchers) {
                boolean isValidDate = true;
                if (v.getNgayBatDau() != null && v.getNgayBatDau().after(now)) {
                    isValidDate = false;
                }
                if (v.getNgayKetThuc() != null && v.getNgayKetThuc().before(now)) {
                    isValidDate = false;
                }

                boolean hasStock = v.getSoLuong() != null && v.getSoLuong() > 0;

                boolean isActive = v.getIsActive() != null && v.getIsActive();
                
                if (isActive && hasStock && isValidDate) {
                    filteredVouchers.add(v);
                }
            }
            
            List<Map<String, Object>> formattedVouchers = new ArrayList<>();
            
            for (Voucher v : filteredVouchers) {
                Map<String, Object> map = new HashMap<>();
                map.put("maVoucher", v.getMaVoucher());
                map.put("tenVoucher", v.getTenVoucher());
                map.put("diemCanDoi", v.getDiemCanDoi());
                map.put("giaTriGiam", v.getGiaTriGiam());
                map.put("donToiThieu", v.getDonToiThieu() != null ? v.getDonToiThieu() : 0);
                map.put("soLuong", v.getSoLuong());
                map.put("ngayBatDau", v.getNgayBatDau());
                map.put("ngayKetThuc", v.getNgayKetThuc());
                formattedVouchers.add(map);
            }
            
            return Map.of("success", true, "vouchers", formattedVouchers);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "Lỗi khi tải danh sách voucher: " + e.getMessage());
        }
    }
    
    @Transactional
    public Map<String, Object> redeemVoucher(Integer maVoucher) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        Optional<Voucher> voucherOpt = voucherDAO.findById(maVoucher);
        if (!voucherOpt.isPresent()) {
            return Map.of("success", false, "message", "Voucher không tồn tại");
        }
        
        Voucher voucher = voucherOpt.get();
        
        if (!voucher.getIsActive() || voucher.getSoLuong() <= 0) {
            return Map.of("success", false, "message", "Voucher đã hết hoặc không còn hiệu lực");
        }
        
        Date now = new Date();
        if (voucher.getNgayBatDau().after(now) || voucher.getNgayKetThuc().before(now)) {
            return Map.of("success", false, "message", "Voucher không trong thời gian áp dụng");
        }
        
        if (customer.getDiemTichLuy() < voucher.getDiemCanDoi()) {
            return Map.of("success", false, "message", "Không đủ điểm để đổi voucher này");
        }
        
        customer.setDiemTichLuy(customer.getDiemTichLuy() - voucher.getDiemCanDoi());
        khachHangDAO.save(customer);
        
        voucher.setSoLuong(voucher.getSoLuong() - 1);
        voucherDAO.save(voucher);

        KhachHangVoucher khv = new KhachHangVoucher();
        khv.setKhachHang(customer);
        khv.setVoucher(voucher);
        khv.setTrangThai("Chưa sử dụng");
        khv.setNgayDoi(now);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, 30);
        khv.setHanSuDung(cal.getTime());
        
        khachHangVoucherDAO.save(khv);

        LichSuTichDiem history = new LichSuTichDiem();
        history.setKhachHang(customer);
        history.setSoDiem(-voucher.getDiemCanDoi());
        history.setLoaiGiaoDich("Đổi voucher");
        history.setNgayGiaoDich(now);
        lichSuTichDiemDAO.save(history);
        
        return Map.of(
            "success", true, 
            "message", "Đổi voucher thành công!", 
            "remainingPoints", customer.getDiemTichLuy()
        );
    }
    
    @Transactional
    public Map<String, Object> applyReferralCode(String referralCode) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập");
        }
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }
        
        if (customer.getMaNguoiGioiThieu() != null && !customer.getMaNguoiGioiThieu().isEmpty()) {
            return Map.of("success", false, "message", "Bạn đã nhập mã giới thiệu trước đó, không thể thay đổi");
        }
        
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return Map.of("success", false, "message", "Vui lòng nhập mã giới thiệu");
        }
        
        referralCode = referralCode.trim().toUpperCase();
        
        if (referralCode.equals(customer.getMaGioiThieu())) {
            return Map.of("success", false, "message", "Bạn không thể nhập mã giới thiệu của chính mình");
        }

        KhachHang referrer = khachHangDAO.findByMaGioiThieu(referralCode);
        if (referrer == null) {
            return Map.of("success", false, "message", "Mã giới thiệu không hợp lệ");
        }
        
        customer.setMaNguoiGioiThieu(referralCode);
        khachHangDAO.save(customer);
        
        Date now = new Date();
        
        customer.setDiemTichLuy(customer.getDiemTichLuy() + 5);
        khachHangDAO.save(customer);
        
        LichSuTichDiem historyForNewUser = new LichSuTichDiem();
        historyForNewUser.setKhachHang(customer);
        historyForNewUser.setSoDiem(5);
        historyForNewUser.setLoaiGiaoDich("Nhập mã giới thiệu");
        historyForNewUser.setNguoiLienQuan(referrer);
        historyForNewUser.setNgayGiaoDich(now);
        lichSuTichDiemDAO.save(historyForNewUser);

        referrer.setDiemTichLuy(referrer.getDiemTichLuy() + 10);
        khachHangDAO.save(referrer);

        LichSuTichDiem historyForReferrer = new LichSuTichDiem();
        historyForReferrer.setKhachHang(referrer);
        historyForReferrer.setSoDiem(10);
        historyForReferrer.setLoaiGiaoDich("Mời bạn bè");
        historyForReferrer.setNguoiLienQuan(customer);
        historyForReferrer.setNgayGiaoDich(now);
        lichSuTichDiemDAO.save(historyForReferrer);
        
        return Map.of(
            "success", true,
            "message", "Nhập mã giới thiệu thành công! Bạn nhận được 5 điểm, người giới thiệu nhận 10 điểm.",
            "newPoints", customer.getDiemTichLuy()
        );
    }
}