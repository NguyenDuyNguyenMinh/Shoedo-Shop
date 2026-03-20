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

            // Tạo đơn hàng trước (trạng thái chờ thanh toán)
            Map<String, Object> checkoutResult = gioHangService.checkout(user, checkoutDTO);
            
            if (!(Boolean) checkoutResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(checkoutResult);
            }

            Integer maHD = (Integer) checkoutResult.get("maHD");
            Double tongTien = (Double) checkoutResult.get("tongTien");
            
            // Chuyển đổi sang VND (không nhân 100 vì amount đã là VND)
            long amount = tongTien.longValue();
            
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

        // Lấy thêm thông tin đơn hàng để truyền về frontend
        String maHD = result.get("maHD");
        String tongTien = result.get("tongTien") != null ? result.get("tongTien") : "0";
        String transactionNo = result.get("transactionNo") != null ? result.get("transactionNo") : "";
        String responseCode = result.get("responseCode") != null ? result.get("responseCode") : "";

        // Chuyển hướng về frontend với kết quả đầy đủ
        String redirectUrl = "http://localhost:4200/payment-result"
            + "?success=" + result.get("success")
            + "&maHD=" + maHD
            + "&tongTien=" + tongTien
            + "&transactionNo=" + transactionNo
            + "&responseCode=" + responseCode
            + "&message=" + (result.get("message") != null
                ? java.net.URLEncoder.encode(result.get("message"), java.nio.charset.StandardCharsets.UTF_8) : "");

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
