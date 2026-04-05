package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Voucher;
import poly.edu.service.VoucherService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voucher")
public class VoucherController {
	@Autowired
    private VoucherService voucherService;


    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllVouchers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Voucher> vouchers = voucherService.findAll();
            response.put("success", true);
            response.put("data", vouchers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi lấy danh sách voucher: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> createVoucher(@RequestBody Voucher voucher) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher savedVoucher = voucherService.create(voucher);
            response.put("success", true);
            response.put("data", savedVoucher);
            response.put("message", "Thêm voucher thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateVoucher(@PathVariable("id") Integer id, @RequestBody Voucher voucher) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher updatedVoucher = voucherService.update(id, voucher);
            response.put("success", true);
            response.put("data", updatedVoucher);
            response.put("message", "Cập nhật voucher thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Map<String, Object>> deactivateVoucher(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher deactivatedVoucher = voucherService.deactivate(id);
            response.put("success", true);
            response.put("data", deactivatedVoucher);
            response.put("message", "Ngừng hoạt động voucher thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    

    @PutMapping("/activate/{id}")
    public ResponseEntity<Map<String, Object>> activateVoucher(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher activatedVoucher = voucherService.activate(id);
            response.put("success", true);
            response.put("data", activatedVoucher);
            response.put("message", "Mở lại voucher thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
