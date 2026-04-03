package poly.edu.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.GioHangDAO;
import poly.edu.dao.HoaDonDAO;
import poly.edu.dao.KhachHangVoucherDAO;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dto.CheckoutDTO;
import poly.edu.entity.HoaDon;
import poly.edu.entity.KhachHangVoucher;
import poly.edu.entity.Users;
import poly.edu.service.AuthService;
import poly.edu.service.GioHangService;
import poly.edu.service.VNPayService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class VNPayController {

    private static final String CART_IDS_PREFIX = "[CART_IDS:";
    private static final String CART_IDS_SUFFIX = "]";

    @Autowired private VNPayService vnPayService;
    @Autowired private GioHangService gioHangService;
    @Autowired private AuthService authService;
    @Autowired private HoaDonDAO hoaDonDAO;
    @Autowired private GioHangDAO gioHangDAO;
    @Autowired private SanPhamChiTietDAO sanPhamChiTietDAO;
    @Autowired private KhachHangVoucherDAO khachHangVoucherDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================================================================
    // 🔴 C1: Khôi phục stock + voucher cho đơn VNPay
    // ================================================================
    @PostMapping("/restore-for-cancel/{maHD}")
    public ResponseEntity<Map<String, Object>> restoreForCancel(@PathVariable Integer maHD) {
        try {
            vnPayService.restoreStockAndVoucherForCancel(maHD);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã khôi phục stock và voucher"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false, "message", "Lỗi khôi phục: " + e.getMessage()));
        }
    }

    // ================================================================
    // 🔴 C4: Xác minh thanh toán từ backend
    // ================================================================
    @GetMapping("/verify-payment/{maHD}")
    public ResponseEntity<Map<String, Object>> verifyPayment(@PathVariable Integer maHD) {
        Optional<HoaDon> hdOpt = hoaDonDAO.findByIdWithDetails(maHD);
        if (hdOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy đơn hàng"));
        }
        HoaDon hd = hdOpt.get();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "maHD", hd.getMaHD(),
            "trangThai", hd.getTrangThai() != null ? hd.getTrangThai() : "",
            "phuongThucTT", hd.getPhuongThucTT() != null ? hd.getPhuongThucTT() : "",
            "ghiChu", hd.getGhiChu() != null ? hd.getGhiChu() : "",
            "daThanhToan", "VNPAY".equals(hd.getPhuongThucTT())
                && !"Đã từ chối".equals(hd.getTrangThai())
        ));
    }

    // ================================================================
    // 🔴 C3: Lưu cartItemIds vào HoaDon.GhiChu (thay vì bảng riêng)
    // ================================================================
    @PostMapping("/create-order")
    @Transactional
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CheckoutDTO checkoutDTO) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            boolean isVNPay = Boolean.TRUE.equals(checkoutDTO.getIsVNPay())
                || "VNPAY".equalsIgnoreCase(checkoutDTO.getPhuongThucTT());

            if (!isVNPay) {
                Map<String, Object> result = gioHangService.checkout(user, checkoutDTO);
                return ResponseEntity.ok(result);
            }

            // checkout tạo HoaDon + HoaDonCT, trừ stock cho VNPay
            Map<String, Object> checkoutResult = gioHangService.checkout(user, checkoutDTO);
            if (!(Boolean) checkoutResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(checkoutResult);
            }

            Integer maHD = (Integer) checkoutResult.get("maHD");
            Double tongTien = (Double) checkoutResult.get("tongTien");
            Double tongTienSauGiam = (Double) checkoutResult.get("tongTienSauGiam");
            Double voucherDiscount = (Double) checkoutResult.get("voucherDiscount");

            long amount = (tongTienSauGiam != null ? tongTienSauGiam.longValue() : tongTien.longValue());
            if (amount <= 0) amount = tongTien.longValue();

            String orderInfo = "Thanh toan don hang #" + maHD;

            // Lưu cartItemIds vào HoaDon.GhiChu (C3)
            saveCartIdsToHoaDon(maHD, checkoutDTO.getCartItemIds());

            String paymentUrl = vnPayService.createPaymentUrl(maHD, amount, orderInfo);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "maHD", maHD,
                "tongTien", amount,
                "tongTienSauGiam", amount,
                "voucherDiscount", voucherDiscount != null ? voucherDiscount : 0.0
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi tạo thanh toán: " + e.getMessage()
            ));
        }
    }

    // ================================================================
    // 🔴 C3: Lưu cartItemIds vào HoaDon.GhiChu (thay vì bảng riêng)
    // ================================================================
    private void saveCartIdsToHoaDon(Integer maHD, List<Integer> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) return;
        hoaDonDAO.findById(maHD).ifPresent(hd -> {
            try {
                String cartIdsJson = CART_IDS_PREFIX
                    + objectMapper.writeValueAsString(cartItemIds)
                    + CART_IDS_SUFFIX;
                String currentNote = hd.getGhiChu() != null ? hd.getGhiChu() : "";
                hd.setGhiChu(cartIdsJson + currentNote);
                hoaDonDAO.save(hd);
            } catch (Exception e) {
                // ignore
            }
        });
    }
    private List<Integer> extractCartItemIdsFromGhiChu(String ghiChu) {
        List<Integer> result = new ArrayList<>();
        if (ghiChu == null || ghiChu.isEmpty()) return result;

        int start = ghiChu.indexOf(CART_IDS_PREFIX);
        if (start == -1) return result;

        int jsonStart = start + CART_IDS_PREFIX.length();
        int end = ghiChu.indexOf(CART_IDS_SUFFIX, jsonStart);
        if (end == -1) return result;

        String json = ghiChu.substring(jsonStart, end);
        try {
            List<Integer> ids = objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
            return ids != null ? ids : result;
        } catch (Exception e) {
            return result;
        }
    }

    // ================================================================
    // Xóa cartItemIds khỏi GhiChu sau khi xử lý xong
    // ================================================================
    private void cleanCartIdsFromGhiChu(HoaDon hd) {
        String ghiChu = hd.getGhiChu();
        if (ghiChu == null || ghiChu.isEmpty()) return;

        int start = ghiChu.indexOf(CART_IDS_PREFIX);
        if (start == -1) return;

        String remaining = ghiChu.substring(0, start).trim();
        hd.setGhiChu(remaining.isEmpty() ? null : remaining);
        hoaDonDAO.save(hd);
    }

    @GetMapping("/vnpay-return")
    @Transactional
    public ResponseEntity<Map<String, String>> vnpayReturn(@RequestParam Map<String, String> params) {
        Map<String, String> result = vnPayService.processReturn(params);
        String success = result.get("success");
        String maHDStr = result.get("maHD");
        String message = result.get("message") != null ? result.get("message") : "";

        if (maHDStr != null) {
            try {
                Integer maHD = Integer.parseInt(maHDStr);

                Optional<HoaDon> hdOpt = hoaDonDAO.findByIdWithDetails(maHD);
                if (hdOpt.isPresent()) {
                    HoaDon hoaDon = hdOpt.get();
                    // Lấy cartItemIds từ GhiChu (C3)
                    List<Integer> cartItemIds = extractCartItemIdsFromGhiChu(hoaDon.getGhiChu());

                    if ("true".equals(success)) {
                        // Xóa cart items
                        for (Integer maGH : cartItemIds) {
                            gioHangDAO.findById(maGH).ifPresent(gioHangDAO::delete);
                        }
                    } else {
                        // Thanh toán thất bại: hoàn stock + voucher
                        if (hoaDon.getHoaDonCTs() != null) {
                            for (var hdct : hoaDon.getHoaDonCTs()) {
                                sanPhamChiTietDAO.congSoLuong(
                                    hdct.getSanPhamChiTiet().getMaSKU(), hdct.getSoLuong());
                            }
                        }
                        KhachHangVoucher khv = hoaDon.getKhachHangVoucher();
                        if (khv != null) {
                            khv.setTrangThai("Chưa sử dụng");
                            khv.setNgayDoi(null);
                            khachHangVoucherDAO.save(khv);
                        }
                    }

                    // Xóa cartIds khỏi GhiChu sau khi xử lý xong (C3)
                    cleanCartIdsFromGhiChu(hoaDon);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String redirectUrl = "http://localhost:5173/payment-result?success=" + success
            + "&maHD=" + (maHDStr != null ? maHDStr : "")
            + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestBody Map<String, String> params) {
        Map<String, String> result = vnPayService.processIpn(params);
        return ResponseEntity.ok(result);
    }

    // ================================================================
    // 🔴 C2: Refund có auth + validate amount
    // ================================================================
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refund(
            @RequestParam String vnp_TransactionNo,
            @RequestParam String vnp_TxnRef,
            @RequestParam long amount,
            @RequestParam String vnp_TransactionDate,
            @RequestParam(required = false) String note) {
        try {
            // Kiểm tra auth
            if (authService.getCurrentUser() == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false, "message", "Vui lòng đăng nhập"));
            }
            if (!authService.isAdmin() && !authService.isEmployee()) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false, "message", "Bạn không có quyền thực hiện chức năng này"));
            }

            // Validate mã đơn hàng
            Integer maHD;
            try {
                maHD = Integer.parseInt(vnp_TxnRef);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", "Mã đơn hàng không hợp lệ"));
            }

            var hdOpt = hoaDonDAO.findByIdWithDetails(maHD);
            if (hdOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", "Không tìm thấy đơn hàng"));
            }
            var hd = hdOpt.get();

            // Tính expected amount từ order thực tế
            double orderTotal = 0;
            if (hd.getHoaDonCTs() != null) {
                for (var ct : hd.getHoaDonCTs()) {
                    orderTotal += ct.getSoLuong() * ct.getDonGia();
                }
            }
            double discount = 0;
            if (hd.getKhachHangVoucher() != null
                    && "Đã sử dụng".equals(hd.getKhachHangVoucher().getTrangThai())) {
                var v = hd.getKhachHangVoucher().getVoucher();
                if (v != null && v.getGiaTriGiam() != null) {
                    discount = v.getGiaTriGiam();
                }
            }
            double expectedAmount = Math.max(0, orderTotal - discount);

            if (amount != (long) expectedAmount && amount != (long) orderTotal) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message",
                    "Số tiền hoàn không hợp lệ. Mong đợi: " + (long) expectedAmount
                        + " VND, nhận được: " + amount + " VND"));
            }

            String res = vnPayService.refund(vnp_TransactionNo, vnp_TxnRef, amount, vnp_TransactionDate, note);
            return ResponseEntity.ok(Map.of("success", true, "result", res));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false, "message", "Lỗi hoàn tiền: " + e.getMessage()));
        }
    }
}
