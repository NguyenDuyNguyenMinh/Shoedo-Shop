package poly.edu.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
import java.util.concurrent.ConcurrentHashMap;

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
    private GioHangDAO gioHangDAO;

    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;

    @Autowired
    private KhachHangVoucherDAO khachHangVoucherDAO;

    @Value("${vnpay.enabled:true}")
    private boolean vnpayEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lưu tạm cartItemIds theo maHD trong bộ nhớ (in-memory).
     * Key = maHD, Value = JSON string của cartItemIds.
     * Dùng ConcurrentHashMap để hỗ trợ truy cập đồng thời an toàn.
     */
    private final Map<Integer, String> pendingCartItemIds = new ConcurrentHashMap<>();

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

            // Tạo đơn hàng trước (trạng thái chờ thanh toán)
            Map<String, Object> checkoutResult = gioHangService.checkout(user, checkoutDTO);

            if (!(Boolean) checkoutResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(checkoutResult);
            }

            Integer maHD = (Integer) checkoutResult.get("maHD");
            Double tongTien = (Double) checkoutResult.get("tongTien");
            Double tongTienSauGiam = (Double) checkoutResult.get("tongTienSauGiam");
            Double voucherDiscount = (Double) checkoutResult.get("voucherDiscount");

            // Số tiền thanh toán thực tế = sau giảm giá voucher (tối thiểu 0)
            long amount = (tongTienSauGiam != null ? tongTienSauGiam.longValue() : tongTien.longValue());
            if (amount <= 0) amount = tongTien.longValue(); // fallback nếu voucher cover hết

            String orderInfo = "Thanh toan don hang #" + maHD;

            // Lưu cartItemIds vào in-memory map để dùng khi VNPAY redirect về
            if (checkoutDTO.getCartItemIds() != null && !checkoutDTO.getCartItemIds().isEmpty()) {
                try {
                    pendingCartItemIds.put(maHD, objectMapper.writeValueAsString(checkoutDTO.getCartItemIds()));
                } catch (Exception e) {
                    // ignore serialization errors
                }
            }

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

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> vnpayReturn(
            @RequestParam Map<String, String> params) {

        Map<String, String> result = vnPayService.processReturn(params);
        String success = result.get("success");
        String maHDStr = result.get("maHD");
        String message = result.get("message") != null ? result.get("message") : "";

        if (maHDStr != null) {
            try {
                Integer maHD = Integer.parseInt(maHDStr);

                // Lấy cartItemIds từ in-memory map
                List<Integer> cartItemIds = new ArrayList<>();
                String storedJson = pendingCartItemIds.remove(maHD); // lấy và xóa luôn
                if (storedJson != null && !storedJson.isEmpty()) {
                    try {
                        cartItemIds = objectMapper.readValue(storedJson,
                            new TypeReference<List<Integer>>() {});
                    } catch (Exception ignored) {}
                }

                Optional<HoaDon> optHD = hoaDonDAO.findByIdWithDetails(maHD);

                if (optHD.isPresent()) {
                    HoaDon hoaDon = optHD.get();

                    if ("true".equals(success)) {
                        // ✅ THANH TOÁN THÀNH CÔNG
                        // Stock đã được trừ tại GioHangService.checkout() (VNPay branch)
                        // Voucher đã được đánh dấu "Đã sử dụng" tại GioHangService.checkout()
                        // Chỉ xóa cart items
                        for (Integer maGH : cartItemIds) {
                            gioHangDAO.findById(maGH).ifPresent(gioHangDAO::delete);
                        }
                    } else {
                        // ❌ THANH TOÁN THẤT BẠI / HỦY
                        // 1) Hoàn lại stock đã trừ (cộng lại vào SanPhamChiTiet)
                        if (hoaDon.getHoaDonCTs() != null) {
                            for (var hdct : hoaDon.getHoaDonCTs()) {
                                sanPhamChiTietDAO.congSoLuong(
                                    hdct.getSanPhamChiTiet().getMaSKU(),
                                    hdct.getSoLuong()
                                );
                            }
                        }
                        // 2) Hủy voucher (khôi phục về "Chưa sử dụng")
                        KhachHangVoucher khv = hoaDon.getKhachHangVoucher();
                        if (khv != null) {
                            khv.setTrangThai("Chưa sử dụng");
                            khv.setNgayDoi(null);
                            khachHangVoucherDAO.save(khv);
                        }
                        // 3) HoaDon giữ nguyên (trạng thái "Đang xử lý") — user có thể retry
                        // Cart items KHÔNG bị xóa → user quay lại checkout thấy lại sản phẩm
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Chuyển hướng về frontend (Vite dev server port 5173)
        String redirectUrl = "http://localhost:5173/payment-result?success=" + success
            + "&maHD=" + (maHDStr != null ? maHDStr : "")
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
