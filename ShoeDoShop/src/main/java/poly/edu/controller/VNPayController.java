package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

            if (!isVNPay) {
                // Nếu không phải VNPAY, xử lý như checkout thông thường (COD)
                Map<String, Object> result = gioHangService.checkout(user, checkoutDTO);
                return ResponseEntity.ok(result);
            }

            // VNPay: Tạo đơn hàng với trạng thái "Chờ thanh toán" (chưa trừ kho)
            Map<String, Object> checkoutResult = gioHangService.createPendingOrder(user, checkoutDTO);

            if (!(Boolean) checkoutResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(checkoutResult);
            }

            Integer maHD = (Integer) checkoutResult.get("maHD");
            Double tongTien = (Double) checkoutResult.get("tongTien");

            // Chuyển đổi sang VND (không nhân 100 vì amount đã là VND)
            long amount = tongTien.longValue();

            String orderInfo = "Thanh toan don hang #" + maHD;

            // Kiểm tra yêu cầu QR code
            boolean isQRCode = Boolean.TRUE.equals(checkoutDTO.getIsQRCode());
            String paymentUrl = vnPayService.createPaymentUrl(maHD, amount, orderInfo, isQRCode);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "maHD", maHD,
                "tongTien", amount,
                "isQRCode", isQRCode
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

        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String maHD = params.get("vnp_TxnRef");

        // Xử lý khi khách hàng hủy thanh toán (mã 24)
        if ("24".equals(vnp_ResponseCode)) {
            // Hủy đơn hàng chờ thanh toán
            if (maHD != null && !maHD.isEmpty()) {
                try {
                    Integer orderId = Integer.parseInt(maHD);
                    gioHangService.cancelPendingOrder(orderId);
                } catch (Exception e) {
                    // Log lỗi nhưng vẫn redirect
                }
            }
            // Redirect về trang đặt hàng
            String frontendUrl = System.getenv("FRONTEND_URL") != null
                ? System.getenv("FRONTEND_URL") : "http://localhost:5173";
            String redirectUrl = frontendUrl + "/customer/checkout?cancelled=true";
            return ResponseEntity.status(302).header("Location", redirectUrl).build();
        }

        // Xử lý các trường hợp khác
        Map<String, String> result = vnPayService.processReturn(params);

        // Nếu thanh toán thành công (mã 00)
        if ("00".equals(vnp_ResponseCode) && maHD != null && !maHD.isEmpty()) {
            try {
                Integer orderId = Integer.parseInt(maHD);
                Map<String, Object> confirmResult = gioHangService.confirmVNPayPayment(orderId);
                result.put("confirmSuccess", confirmResult.get("success").toString());
            } catch (Exception e) {
                result.put("confirmSuccess", "false");
            }
        }

        // Chuyển hướng về frontend với kết quả
        String frontendUrl = System.getenv("FRONTEND_URL") != null
            ? System.getenv("FRONTEND_URL") : "http://localhost:5173";
        String redirectUrl = frontendUrl + "/customer/checkout?success=" + result.get("success")
            + "&maHD=" + (maHD != null ? maHD : "")
            + "&message=" + (result.get("message") != null ? result.get("message").replace(" ", "%20") : "");

        // Redirect về frontend
        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestBody Map<String, String> params) {
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
