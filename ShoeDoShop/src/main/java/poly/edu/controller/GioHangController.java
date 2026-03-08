package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.CheckoutDTO;
import poly.edu.dto.GioHangDTO;
import poly.edu.entity.Users;
import poly.edu.service.AuthService;
import poly.edu.service.GioHangService;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private AuthService authService;

    /**
     * Lấy giỏ hàng
     */
    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> getCart() {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"));
            }
            Map<String, Object> result = gioHangService.getCart(user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy giỏ hàng: " + e.getMessage()));
        }
    }

    /**
     * Thêm vào giỏ hàng
     */
    @PostMapping("/cart")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody GioHangDTO dto) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"));
            }
            Map<String, Object> result = gioHangService.addToCart(user, dto);
            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi thêm vào giỏ hàng: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật số lượng
     */
    @PutMapping("/cart/{maGH}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable("maGH") Integer maGH,
            @RequestBody Map<String, Integer> body) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"));
            }
            Integer soLuong = body.get("soLuong");
            Map<String, Object> result = gioHangService.updateCartItem(user, maGH, soLuong);
            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi cập nhật giỏ hàng: " + e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/cart/{maGH}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable("maGH") Integer maGH) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"));
            }
            Map<String, Object> result = gioHangService.removeFromCart(user, maGH);
            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Lấy số lượng giỏ hàng
     */
    @GetMapping("/cart/count")
    public ResponseEntity<Map<String, Object>> getCartCount() {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.ok(Map.of("success", true, "cartCount", 0));
            }
            Map<String, Object> result = gioHangService.getCartCount(user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", true, "cartCount", 0));
        }
    }

    /**
     * Đặt hàng (checkout)
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody CheckoutDTO dto) {
        try {
            Users user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"));
            }
            Map<String, Object> result = gioHangService.checkout(user, dto);
            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi đặt hàng: " + e.getMessage()));
        }
    }
}
