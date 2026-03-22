package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.HoaDonDAO;
import poly.edu.dao.HoaDonCTDAO;
import poly.edu.dto.CheckoutDTO;
import poly.edu.entity.HoaDon;
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

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;

    /** Frontend base URL — dùng để redirect về trang kết quả thanh toán */
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

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
                // Không phải VNPAY → xử lý COD
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
            long amount = tongTien.longValue();
            String orderInfo = "Thanh toan don hang #" + maHD;

            // Redirect sang VNPay
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
        // Sử dụng frontendUrl từ config — không còn hardcoded port 4200
        String redirectUrl = frontendUrl + "/payment-result"
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

    /**
     * Verify payment — chống spoof URL params trên frontend (PaymentResult.vue).
     * LUÔN gọi backend để xác nhận đơn hàng thực sự đã thanh toán VNPay.
     */
    @GetMapping("/verify/{maHD}")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @PathVariable Integer maHD,
            @RequestParam(required = false) String transactionNo) {

        HoaDon hoaDon = hoaDonDAO.findById(maHD).orElse(null);
        if (hoaDon == null) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Không tìm thấy đơn hàng"
            ));
        }

        boolean valid = "VNPAY".equals(hoaDon.getPhuongThucTT())
                && hoaDon.getGhiChu() != null
                && (transactionNo == null || hoaDon.getGhiChu().contains(transactionNo));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "valid", valid,
            "trangThai", hoaDon.getTrangThai(),
            "phuongThucTT", hoaDon.getPhuongThucTT() != null ? hoaDon.getPhuongThucTT() : "",
            "tongTien", hoaDonCTDAO.findByHoaDon_MaHD(maHD).stream()
                .mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia())
                .sum()
        ));
    }
}
