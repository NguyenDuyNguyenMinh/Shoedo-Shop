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

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private AuthService authService;

    /** Frontend URL đọc từ application.properties thay vì hardcode */
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CheckoutDTO checkoutDTO) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
            }

            boolean isVNPay = Boolean.TRUE.equals(checkoutDTO.getIsVNPay())
                || "VNPAY".equalsIgnoreCase(checkoutDTO.getPhuongThucTT());

            if (!isVNPay) {
                // COD — xử lý checkout thông thường
                Map<String, Object> result = gioHangService.checkout(user, checkoutDTO);
                return ResponseEntity.ok(result);
            }

            // VNPay — tạo payment URL
            // checkout() đã trừ kho + tạo HoaDon rồi (trong create-order)
            Map<String, Object> checkoutResult = gioHangService.checkout(user, checkoutDTO);

            if (!(Boolean) checkoutResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(checkoutResult);
            }

            Integer maHD = (Integer) checkoutResult.get("maHD");
            Double tongTien = (Double) checkoutResult.get("tongTien");

            // Nhân 100 theo chuẩn VNPay (amount tính bằng cents/xu)
            long amount = tongTien != null ? tongTien.longValue() : 0L;
            String orderInfo = "Thanh toan don hang #" + maHD;

            String paymentUrl = vnPayService.createPaymentUrl(maHD, amount, orderInfo);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "maHD", maHD,
                "tongTien", amount
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

        // Redirect về frontend với kết quả VNPay
        String success = result.getOrDefault("success", "false");
        String maHD = result.getOrDefault("maHD", "");
        String responseCode = result.getOrDefault("responseCode", "");

        String redirectUrl = frontendUrl + "/payment-result"
            + "?vnp_ResponseCode=" + (responseCode != null ? responseCode : "")
            + "&vnp_TxnRef=" + (maHD != null ? maHD : "")
            + "&success=" + success;

        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestBody Map<String, String> params) {
        Map<String, String> result = vnPayService.processIpn(params);
        return ResponseEntity.ok(result);
    }

    /**
     * Hoàn tiền — CHỈ nhân viên hoặc admin được phép.
     * Role check được thực hiện bởi AuthInterceptor qua path-based guard.
     */
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refund(
            @RequestParam String vnp_TransactionNo,
            @RequestParam String vnp_TxnRef,
            @RequestParam long amount,
            @RequestParam String vnp_TransactionDate,
            @RequestParam(required = false) String note) {

        // Kiểm tra role server-side — chỉ EMPLOYEE hoặc ADMIN
        Users user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false, "message", "Vui lòng đăng nhập"
            ));
        }
        // QuanTri.role là Boolean: null = customer, false = EMPLOYEE, true = ADMIN
        Boolean role = user.getQuanTri() != null ? user.getQuanTri().getRole() : null;
        boolean isStaff = role != null && role; // ADMIN=true hoặc EMPLOYEE=false đều là staff
        if (!isStaff) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false, "message", "Chỉ nhân viên hoặc quản trị viên mới được thực hiện hoàn tiền"
            ));
        }

        try {
            // Restore stock khi refund thành công
            String refundResult = vnPayService.refund(vnp_TransactionNo, vnp_TxnRef, amount, vnp_TransactionDate, note);
            vnPayService.restoreStockOnRefund(vnp_TxnRef);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "result", refundResult
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi hoàn tiền: " + e.getMessage()
            ));
        }
    }
}
