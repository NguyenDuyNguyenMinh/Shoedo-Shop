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
     * Báo lỗi đơn hàng
     */
    @PostMapping("/report-issue")
    public ResponseEntity<Map<String, Object>> reportIssue(@RequestBody Map<String, Object> request) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.reportIssue(request, currentUser);

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
                    "message", "Lỗi khi xử lý báo lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Đánh giá sản phẩm
     */
    @PostMapping("/review")
    public ResponseEntity<Map<String, Object>> addReview(@RequestBody Map<String, Object> request) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.addReview(request, currentUser);

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
                    "message", "Lỗi khi đánh giá sản phẩm: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy đánh giá của chi tiết đơn hàng
     */
    @GetMapping("/review/{maHDCT}")
    public ResponseEntity<Map<String, Object>> getReview(@PathVariable("maHDCT") Integer maHDCT) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.getReview(maHDCT, currentUser);
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
                    "message", "Lỗi khi lấy đánh giá: " + e.getMessage()
            ));
        }
    }

    /**
     * Hủy đơn hàng với lý do
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable("orderId") Integer orderId,
            @RequestBody Map<String, String> request) {
        try {
            // Kiểm tra đăng nhập
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để hủy đơn hàng"
                ));
            }

            // Lấy lý do hủy từ request
            String cancelReason = request.get("cancelReason");

            // Validate lý do hủy
            if (cancelReason == null || cancelReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng chọn lý do hủy đơn hàng"
                ));
            }

            // Gọi service xử lý hủy đơn
            Map<String, Object> result = donHangService.cancelOrder(orderId, cancelReason, currentUser);

            // Kiểm tra kết quả
            if (!(boolean) result.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", result.get("message")
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.get("message"),
                    "order", result.get("order")
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi hủy đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Chỉnh sửa đánh giá sản phẩm
     */
    @PutMapping("/review")
    public ResponseEntity<Map<String, Object>> updateReview(@RequestBody Map<String, Object> request) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Map<String, Object> result = donHangService.updateReview(request, currentUser);

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
                    "message", "Lỗi khi cập nhật đánh giá: " + e.getMessage()
            ));
        }
    }
}