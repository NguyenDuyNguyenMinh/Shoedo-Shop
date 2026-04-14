package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.*;
import poly.edu.entity.*;

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
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", Map.of(
            "maUser", currentUser.getMaUser(),
            "userName", currentUser.getUserName(),
            "mail", currentUser.getMail(),
            "isActive", currentUser.getIsActive()
        ));
        response.put("customer", Map.of(
            "maKH", customer.getMaKH(),
            "tenKH", customer.getTenKH(),
            "sdt", customer.getSdt(),
            "diemTichLuy", customer.getDiemTichLuy() != null ? customer.getDiemTichLuy() : 0,
            "maGioiThieu", customer.getMaGioiThieu(),
            "hasAppliedReferral", customer.getMaNguoiGioiThieu() != null && !customer.getMaNguoiGioiThieu().isEmpty()
        ));
        response.put("addresses", diaChiDAO.findByKhachHang_MaKH(customer.getMaKH()));
        return response;
    }
    
    public Map<String, Object> updateProfile(Map<String, String> request) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        String userName = request.get("userName");
        String fullname = request.get("fullname");
        String phone = request.get("phone");

        if (userName != null) {
            Users existing = usersDAO.findByUserName(userName);
            if (existing != null && !existing.getMaUser().equals(currentUser.getMaUser())) {
                return error("Tên đăng nhập đã được sử dụng");
            }
            currentUser.setUserName(userName);
            usersDAO.save(currentUser);
        }

        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        KhachHang existing = khachHangDAO.findBySdt(phone);
        if (existing != null && !existing.getMaKH().equals(customer.getMaKH())) {
            return error("Số điện thoại đã được sử dụng");
        }
        
        customer.setTenKH(fullname);
        customer.setSdt(phone);
        khachHangDAO.save(customer);
        
        return success("Cập nhật thông tin thành công!", Map.of(
            "user", Map.of("userName", currentUser.getUserName()),
            "customer", Map.of("tenKH", customer.getTenKH(), "sdt", customer.getSdt())
        ));
    }
    
    public Map<String, Object> changePassword(Map<String, String> request) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        String result = authService.changePassword(
            currentUser.getMail(), 
            request.get("currentPassword"), 
            request.get("newPassword"), 
            request.get("confirmPassword")
        );
        
        return result.equals("OK") ? success("Đổi mật khẩu thành công!") : error(result);
    }
    
    public Map<String, Object> getAddresses() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        List<DiaChi> addresses = diaChiDAO.findByKhachHang_MaKH(customer.getMaKH());
        addresses.sort((a, b) -> Boolean.compare(b.getMacDinh(), a.getMacDinh()));
        return Map.of("success", true, "addresses", addresses);
    }
    
    public Map<String, Object> addAddress(Map<String, Object> request) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        Boolean macDinh = request.get("macDinh") != null ? (Boolean) request.get("macDinh") : false;
        if (macDinh) diaChiDAO.clearDefaultAddress(customer.getMaKH());
        
        DiaChi address = new DiaChi();
        address.setKhachHang(customer);
        address.setTenNN(((String) request.get("tenNN")).trim());
        address.setSdt(((String) request.get("sdt")).trim());
        address.setDiemGiao(((String) request.get("diemGiao")).trim());
        address.setMacDinh(macDinh);
        diaChiDAO.save(address);
        
        return success("Thêm địa chỉ thành công!");
    }
    
    public Map<String, Object> updateAddress(Integer id, Map<String, Object> request) {
        Optional<DiaChi> optional = diaChiDAO.findById(id);
        if (!optional.isPresent()) return error("Không tìm thấy địa chỉ");
        
        DiaChi address = optional.get();
        Boolean macDinh = request.get("macDinh") != null ? (Boolean) request.get("macDinh") : address.getMacDinh();
        
        if (macDinh && !address.getMacDinh()) {
            diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
        }
        
        address.setTenNN(((String) request.get("tenNN")).trim());
        address.setSdt(((String) request.get("sdt")).trim());
        address.setDiemGiao(((String) request.get("diemGiao")).trim());
        address.setMacDinh(macDinh);
        diaChiDAO.save(address);
        
        return success("Cập nhật địa chỉ thành công!");
    }
    
    public Map<String, Object> deleteAddress(Integer id) {
        Optional<DiaChi> optional = diaChiDAO.findById(id);
        if (!optional.isPresent()) return error("Không tìm thấy địa chỉ");
        
        if (optional.get().getMacDinh()) return error("Không thể xóa địa chỉ mặc định");
        
        diaChiDAO.deleteById(id);
        return success("Xóa địa chỉ thành công!");
    }
    
    public Map<String, Object> setDefaultAddress(Integer id) {
        Optional<DiaChi> optional = diaChiDAO.findById(id);
        if (!optional.isPresent()) return error("Không tìm thấy địa chỉ");
        
        DiaChi address = optional.get();
        diaChiDAO.clearDefaultAddress(address.getKhachHang().getMaKH());
        address.setMacDinh(true);
        diaChiDAO.save(address);
        
        return success("Đặt địa chỉ mặc định thành công!");
    }
    
    public Map<String, Object> getPointsHistory() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        List<Map<String, Object>> history = lichSuTichDiemDAO.findByKhachHangOrderByNgayGiaoDichDesc(customer)
            .stream().map(item -> {
                Map<String, Object> map = new HashMap<>();
                map.put("soDiem", item.getSoDiem());
                map.put("loaiGiaoDich", item.getLoaiGiaoDich());
                map.put("ngayGiaoDich", item.getNgayGiaoDich());
                return map;
            }).collect(Collectors.toList());
        
        return Map.of("success", true, "history", history, "currentPoints", customer.getDiemTichLuy());
    }
    
    public Map<String, Object> getMyVouchers() {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        List<Map<String, Object>> vouchers = khachHangVoucherDAO.findByKhachHang(customer)
            .stream().map(item -> {
                Map<String, Object> map = new HashMap<>();
                map.put("maKHVC", item.getMaKHVC());
                map.put("voucher", item.getVoucher());
                map.put("trangThai", item.getTrangThai());
                map.put("ngayDoi", item.getNgayDoi());
                map.put("hanSuDung", item.getHanSuDung());
                return map;
            }).collect(Collectors.toList());
        
        return Map.of("success", true, "vouchers", vouchers);
    }
    
    public Map<String, Object> getAvailableVouchers() {
        Date now = new Date();
        List<Voucher> vouchers = voucherDAO.findAvailableVouchers(now);
        
        List<Map<String, Object>> formatted = vouchers.stream()
            .filter(v -> v.getIsActive() && v.getSoLuong() > 0)
            .map(v -> {
                Map<String, Object> map = new HashMap<>();
                map.put("maVoucher", v.getMaVoucher());
                map.put("tenVoucher", v.getTenVoucher());
                map.put("diemCanDoi", v.getDiemCanDoi());
                map.put("giaTriGiam", v.getGiaTriGiam());
                map.put("donToiThieu", v.getDonToiThieu() != null ? v.getDonToiThieu() : 0);
                map.put("soLuong", v.getSoLuong());
                map.put("ngayBatDau", v.getNgayBatDau());
                map.put("ngayKetThuc", v.getNgayKetThuc());
                return map;
            }).collect(Collectors.toList());
        
        return Map.of("success", true, "vouchers", formatted);
    }
    
    @Transactional
    public Map<String, Object> redeemVoucher(Integer maVoucher) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        Optional<Voucher> voucherOpt = voucherDAO.findById(maVoucher);
        if (!voucherOpt.isPresent()) return error("Voucher không tồn tại");
        
        Voucher voucher = voucherOpt.get();
        Date now = new Date();
        
        if (!voucher.getIsActive() || voucher.getSoLuong() <= 0) return error("Voucher đã hết hoặc không còn hiệu lực");
        if (voucher.getNgayBatDau().after(now) || voucher.getNgayKetThuc().before(now)) return error("Voucher không trong thời gian áp dụng");
        if (customer.getDiemTichLuy() < voucher.getDiemCanDoi()) return error("Không đủ điểm để đổi voucher này");

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
        
        return Map.of("success", true, "message", "Đổi voucher thành công!", "remainingPoints", customer.getDiemTichLuy());
    }
    
    @Transactional
    public Map<String, Object> applyReferralCode(String referralCode) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        if (customer.getMaNguoiGioiThieu() != null && !customer.getMaNguoiGioiThieu().isEmpty()) {
            return error("Bạn đã nhập mã giới thiệu trước đó, không thể thay đổi");
        }
        
        if (referralCode == null || referralCode.trim().isEmpty()) return error("Vui lòng nhập mã giới thiệu");
        
        referralCode = referralCode.trim();
        if (referralCode.equals(customer.getMaGioiThieu())) return error("Bạn không thể nhập mã giới thiệu của chính mình");
        
        KhachHang referrer = khachHangDAO.findByMaGioiThieu(referralCode);
        if (referrer == null || !referrer.getMaGioiThieu().equals(referralCode)) return error("Mã giới thiệu không hợp lệ");
        
        customer.setMaNguoiGioiThieu(referralCode);
        khachHangDAO.save(customer);
        
        Date now = new Date();

        customer.setDiemTichLuy(customer.getDiemTichLuy() + 3);
        khachHangDAO.save(customer);
        
        LichSuTichDiem historyNew = new LichSuTichDiem();
        historyNew.setKhachHang(customer);
        historyNew.setSoDiem(3);
        historyNew.setLoaiGiaoDich("Nhập mã giới thiệu");
        historyNew.setNgayGiaoDich(now);
        lichSuTichDiemDAO.save(historyNew);
        
        referrer.setDiemTichLuy(referrer.getDiemTichLuy() + 5);
        khachHangDAO.save(referrer);
        
        LichSuTichDiem historyRef = new LichSuTichDiem();
        historyRef.setKhachHang(referrer);
        historyRef.setSoDiem(5);
        historyRef.setLoaiGiaoDich("Mời bạn bè");
        historyRef.setNgayGiaoDich(now);
        lichSuTichDiemDAO.save(historyRef);
        
        return Map.of("success", true, "message", "Nhập mã giới thiệu thành công! Bạn nhận được 3 điểm.", "newPoints", customer.getDiemTichLuy());
    }
    
    public Map<String, Object> deleteExpiredVoucher(Integer maKHVC) {
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        Optional<KhachHangVoucher> khvOpt = khachHangVoucherDAO.findById(maKHVC);
        if (!khvOpt.isPresent()) return error("Không tìm thấy voucher");
        
        KhachHangVoucher khv = khvOpt.get();

        if (!khv.getKhachHang().getMaKH().equals(customer.getMaKH())) {
            return error("Bạn không có quyền xóa voucher này");
        }

        if (!"Hết hạn".equals(khv.getTrangThai())) {
            return error("Chỉ có thể xóa voucher đã hết hạn");
        }
        
        khachHangVoucherDAO.delete(khv);
        return success("Xóa voucher thành công!");
    }

    @Transactional
    public Map<String, Object> deleteBatchExpiredVouchers(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return error("Không có voucher nào được chọn");
        
        Users currentUser = authService.getCurrentUser();
        if (currentUser == null) return error("Chưa đăng nhập");
        
        KhachHang customer = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (customer == null) return error("Không tìm thấy thông tin khách hàng");
        
        int deletedCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Integer id : ids) {
            try {
                Optional<KhachHangVoucher> khvOpt = khachHangVoucherDAO.findById(id);
                if (khvOpt.isPresent()) {
                    KhachHangVoucher khv = khvOpt.get();
                    if (khv.getKhachHang().getMaKH().equals(customer.getMaKH()) && 
                        "Hết hạn".equals(khv.getTrangThai())) {
                        khachHangVoucherDAO.delete(khv);
                        deletedCount++;
                    } else {
                        errors.add("Voucher ID " + id + " không hợp lệ");
                    }
                }
            } catch (Exception e) {
                errors.add("Lỗi khi xóa voucher ID " + id);
            }
        }
        
        if (deletedCount > 0) {
            return success("Đã xóa " + deletedCount + " voucher thành công!" + 
                          (errors.isEmpty() ? "" : " (" + errors.size() + " lỗi)"));
        } else {
            return error("Không thể xóa voucher nào: " + String.join(", ", errors));
        }
    }

    private Map<String, Object> success(String message) { return Map.of("success", true, "message", message); }
    private Map<String, Object> success(String message, Object data) { 
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", message);
        res.putAll((Map) data);
        return res;
    }
    private Map<String, Object> error(String message) { return Map.of("success", false, "message", message); }
}