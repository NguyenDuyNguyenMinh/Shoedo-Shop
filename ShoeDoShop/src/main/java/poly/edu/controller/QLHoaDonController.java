package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.QLHoaDonService;

import java.util.Map;

@RestController
@RequestMapping("/api/employee/orders")
public class QLHoaDonController {

    @Autowired private QLHoaDonService orderService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(
            @RequestParam(defaultValue = "Đang xử lý") String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderDetail(id));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PostMapping("/{id}/failed")
    public ResponseEntity<Map<String, Object>> rejectOrder(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(orderService.rejectOrder(id, payload));
    }

    @PostMapping("/{id}/delivery-failed")
    public ResponseEntity<Map<String, Object>> deliveryFailed(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(orderService.deliveryFailed(id, payload));
    }

    @PostMapping("/{id}/delivery-success")
    public ResponseEntity<Map<String, Object>> deliverySuccess(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.deliverySuccess(id));
    }

    @PostMapping("/{id}/confirm-return")
    public ResponseEntity<Map<String, Object>> confirmReturn(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.confirmReturn(id));
    }

    @PostMapping("/{id}/send-apology-email")
    public ResponseEntity<Map<String, Object>> sendApologyEmail(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.sendApologyEmail(id));
    }

    @GetMapping("/{id}/print")
    public ResponseEntity<?> printInvoice(@PathVariable Integer id) {
        return orderService.printInvoice(id);
    }

    @GetMapping("/listnv")
    public ResponseEntity<Map<String, Object>> getAllEmployees() {
        return ResponseEntity.ok(orderService.getAllEmployees());
    }
}
