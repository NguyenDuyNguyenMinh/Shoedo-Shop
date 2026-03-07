package poly.edu.controller.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.checkout.CheckoutRequestDTO;
import poly.edu.dto.checkout.CheckoutResponseDTO;
import poly.edu.service.checkout.CheckoutService;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    /**
     * POST /api/checkout/process
     * Thực hiện đặt hàng (Checkout) với phương thức COD
     *
     * Body request:
     * {
     *   "maKH": 1,
     *   "maDC": 5,
     *   "ghiChu": "Giao hàng vào giờ hành chính",
     *   "danhSachMaSKU": [1, 2, 3]  // optional - nếu không truyền sẽ mua tất cả trong giỏ
     * }
     */
    @PostMapping("/process")
    public ResponseEntity<CheckoutResponseDTO> processCheckout(@RequestBody CheckoutRequestDTO request) {
        try {
            CheckoutResponseDTO response = checkoutService.processCheckout(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            CheckoutResponseDTO errorResponse = new CheckoutResponseDTO(
                    null, null, null, null, null, "Lỗi: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            CheckoutResponseDTO errorResponse = new CheckoutResponseDTO(
                    null, null, null, null, null, "Lỗi hệ thống: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
