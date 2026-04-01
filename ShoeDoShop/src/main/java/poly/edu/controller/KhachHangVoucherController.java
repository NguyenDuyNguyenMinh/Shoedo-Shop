package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.KhachHangDAO;
import poly.edu.dao.KhachHangVoucherDAO;
import poly.edu.entity.KhachHangVoucher;
import poly.edu.entity.Users;
import poly.edu.service.AuthService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
public class KhachHangVoucherController {

    @Autowired
    private KhachHangVoucherDAO khachHangVoucherDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private AuthService authService;

    /**
     * GET /api/customer/vouchers
     * Trả về danh sách voucher còn hiệu lực của khách hàng đang đăng nhập.
     */
    @GetMapping("/vouchers")
    public ResponseEntity<Map<String, Object>> getMyVouchers() {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"));
            }

            var kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
            if (kh == null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "vouchers", Collections.emptyList()));
            }

            List<KhachHangVoucher> validVouchers =
                    khachHangVoucherDAO.findValidVouchersByMaKH(kh.getMaKH());

            List<Map<String, Object>> data = validVouchers.stream().map(khv -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("maKH_VC",    khv.getMaKHVC());
                map.put("hanSuDung",  khv.getHanSuDung());
                map.put("trangThai",  khv.getTrangThai());

                var v = khv.getVoucher();
                if (v != null) {
                    map.put("maVoucher",    v.getMaVoucher());
                    map.put("tenVoucher",   v.getTenVoucher());
                    map.put("giaTriGiam",   v.getGiaTriGiam());
                    map.put("donToiThieu",  v.getDonToiThieu());
                    map.put("ngayBatDau",   v.getNgayBatDau());
                    map.put("ngayKetThuc",  v.getNgayKetThuc());
                    map.put("isActive",     v.getIsActive());
                }
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success",  true,
                    "vouchers", data));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy voucher: " + e.getMessage()));
        }
    }
}
