package poly.edu.controller;

import poly.edu.dto.KhuyenMaiUpdateRequest;
import poly.edu.entity.SanPham;
import poly.edu.service.KhuyenMaiService;
import poly.edu.dto.SanPhamKhuyenMaiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khuyenmai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true") 
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    // GET: http://localhost:8080/api/khuyenmai/sanpham

    @GetMapping("/sanpham")
    public ResponseEntity<List<SanPhamKhuyenMaiResponse>> getAllSanPham() {
        // Gọi hàm mới trong Service
        return ResponseEntity.ok(khuyenMaiService.getDanhSachKhuyenMai());
    }

    // POST: http://localhost:8080/api/khuyenmai/cap-nhat
    @PostMapping("/cap-nhat")
    public ResponseEntity<?> capNhatKhuyenMai(@RequestBody KhuyenMaiUpdateRequest request) {
        try {
            khuyenMaiService.capNhatKhuyenMai(request);
            return ResponseEntity.ok("Cập nhật khuyến mãi thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // POST: http://localhost:8080/api/khuyenmai/cap-nhat-hang-loat
    @PostMapping("/cap-nhat-hang-loat")
    public ResponseEntity<?> capNhatHangLoat(@RequestBody List<KhuyenMaiUpdateRequest> requests) {
        try {
            khuyenMaiService.capNhatKhuyenMaiHangLoat(requests);
            return ResponseEntity.ok("Cập nhật khuyến mãi hàng loạt thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}