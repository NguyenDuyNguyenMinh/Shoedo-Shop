package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Users;
import poly.edu.service.AuthService;
import poly.edu.service.DonHangService;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/orders")
public class DonHangController {

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private AuthService authService;

    /**
     * Lấy tất cả đơn hàng của khách hàng hiện tại
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyOrders() {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để xem đơn hàng"
                ));
            }

            Map<String, Object> result = donHangService.getMyOrders(currentUser);

            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy chi tiết đơn hàng theo mã
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable("orderId") Integer orderId) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để xem chi tiết đơn hàng"
                ));
            }

            Map<String, Object> result = donHangService.getOrderDetail(orderId, currentUser);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy đơn hàng theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable("status") String status) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.getOrdersByStatus(status, currentUser);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng (xác nhận đã nhận)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable("orderId") Integer orderId,
            @RequestParam("status") String status) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.updateOrderStatus(orderId, status, currentUser);

            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi cập nhật trạng thái: " + e.getMessage()
            ));
        }
    }

    /**
     * Yêu cầu trả hàng
     */
    @PostMapping("/return")
    public ResponseEntity<Map<String, Object>> requestReturn(@RequestBody Map<String, Object> request) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.requestReturn(request, currentUser);

            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi xử lý yêu cầu trả hàng: " + e.getMessage()
            ));
        }
    }
}