package poly.edu.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.CheckoutDTO;
import poly.edu.entity.Users;
import poly.edu.service.AuthService;
import poly.edu.service.GioHangService;
import poly.edu.service.VNPayService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/payment")
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private AuthService authService;

    @Value("${vnpay.enabled:true}")
    private boolean vnpayEnabled;

    /**
     * Lưu tạm dữ liệu checkout theo tempRef trong bộ nhớ (in-memory).
     * Key = tempRef (mã tham chiếu tạm), Value = PendingOrder (CheckoutDTO + maUser).
     * HoaDon chỉ được tạo khi thanh toán VNPay thành công.
     */
    private static class PendingOrder {
        CheckoutDTO checkoutDTO;
        Integer maUser;
        PendingOrder(CheckoutDTO dto, Integer maUser) {
            this.checkoutDTO = dto;
            this.maUser = maUser;
        }
    }

    private final Map<Integer, PendingOrder> pendingOrders = new ConcurrentHashMap<>();
    private final AtomicInteger tempRefCounter = new AtomicInteger(900000);

    @GetMapping("/vnpay-status")
    public ResponseEntity<Map<String, Object>> getVnpayStatus() {
        return ResponseEntity.ok(Map.of(
            "enabled", vnpayEnabled,
            "name", "VNPay",
            "message", vnpayEnabled ? "VNPay đang hoạt động" : "VNPay hiện đang bảo trì, vui lòng chọn phương thức thanh toán khác"
        ));
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CheckoutDTO checkoutDTO) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            // Kiểm tra nếu là thanh toán VNPAY
            boolean isVNPay = Boolean.TRUE.equals(checkoutDTO.getIsVNPay())
                || "VNPAY".equalsIgnoreCase(checkoutDTO.getPhuongThucTT());

            // Guard: chặn nếu VNPay đang bảo trì
            if (isVNPay && !vnpayEnabled) {
                return ResponseEntity.status(503).body(Map.of(
                    "success", false,
                    "message", "VNPay hiện đang bảo trì, vui lòng chọn phương thức thanh toán khác."
                ));
            }

            if (!isVNPay) {
                // Nếu không phải VNPAY, xử lý như checkout thông thường (COD)
                Map<String, Object> result = gioHangService.checkout(user, checkoutDTO);
                return ResponseEntity.ok(result);
            }

            // ── VNPAY: Chỉ validate, KHÔNG tạo HoaDon ──
            Map<String, Object> validationResult = gioHangService.validateCheckout(user, checkoutDTO);

            if (!(Boolean) validationResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(validationResult);
            }

            Double tongTien = (Double) validationResult.get("tongTien");
            Double tongTienSauGiam = (Double) validationResult.get("tongTienSauGiam");
            Double voucherDiscount = (Double) validationResult.get("voucherDiscount");

            // Số tiền thanh toán thực tế = sau giảm giá voucher (tối thiểu 0)
            long amount = (tongTienSauGiam != null ? tongTienSauGiam.longValue() : tongTien.longValue());
            if (amount <= 0) amount = tongTien.longValue(); // fallback nếu voucher cover hết

            // Tạo mã tham chiếu tạm (dùng làm vnp_TxnRef)
            int tempRef = tempRefCounter.incrementAndGet();

            // Lưu dữ liệu checkout vào in-memory để dùng khi VNPay redirect về
            pendingOrders.put(tempRef, new PendingOrder(checkoutDTO, user.getMaUser()));

            String orderInfo = "Thanh toan don hang TEMP" + tempRef;
            String paymentUrl = vnPayService.createPaymentUrl(tempRef, amount, orderInfo);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "tempRef", tempRef,
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

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> vnpayReturn(
            @RequestParam Map<String, String> params) {

        Map<String, String> result = vnPayService.processReturn(params);
        String success = result.get("success");
        String txnRef = result.get("maHD"); // Đây là tempRef, không phải maHD thật
        String message = result.get("message") != null ? result.get("message") : "";

        Integer realMaHD = null;

        if (txnRef != null) {
            try {
                Integer tempRef = Integer.parseInt(txnRef);

                if ("true".equals(success)) {
                    // ✅ THANH TOÁN THÀNH CÔNG → Bây giờ mới tạo HoaDon
                    PendingOrder pending = pendingOrders.remove(tempRef);
                    if (pending != null) {
                        // Tạo Users tạm với maUser để gọi checkout
                        Users user = new Users();
                        user.setMaUser(pending.maUser);

                        // Gọi checkout() → tạo HoaDon thật với trạng thái "Đang xử lý"
                        Map<String, Object> checkoutResult = gioHangService.checkout(user, pending.checkoutDTO);

                        if ((Boolean) checkoutResult.getOrDefault("success", false)) {
                            realMaHD = (Integer) checkoutResult.get("maHD");
                            message = "Thanh toán thành công";
                        } else {
                            // Checkout thất bại (ví dụ: hết hàng trong lúc thanh toán)
                            success = "false";
                            message = (String) checkoutResult.getOrDefault("message", "Lỗi tạo đơn hàng sau thanh toán");
                        }
                    } else {
                        success = "false";
                        message = "Không tìm thấy dữ liệu đơn hàng. Vui lòng liên hệ hỗ trợ.";
                    }
                } else {
                    // ❌ THANH TOÁN THẤT BẠI / HỦY
                    // Không có gì cần rollback vì HoaDon chưa được tạo
                    pendingOrders.remove(tempRef);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Chuyển hướng về frontend (Vite dev server port 5173)
        String redirectUrl = "http://localhost:5173/payment-result?success=" + success
            + "&maHD=" + (realMaHD != null ? realMaHD : "")
            + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        Map<String, String> result = vnPayService.processIpn(params);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refund(
            @RequestParam String vnp_TransactionNo,
            @RequestParam String vnp_TxnRef,
            @RequestParam long amount,
            @RequestParam String vnp_TransactionDate,
            @RequestParam(required = false) String note) {

        try {
            String result = vnPayService.refund(vnp_TransactionNo, vnp_TxnRef, amount, vnp_TransactionDate, note);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi hoàn tiền: " + e.getMessage()
            ));
        }
    }
}
