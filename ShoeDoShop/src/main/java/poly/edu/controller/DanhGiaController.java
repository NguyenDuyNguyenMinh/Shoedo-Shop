package poly.edu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.ApiResponse;
import poly.edu.dto.DanhGiaDTO;
import poly.edu.service.DanhGiaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/danh-gia")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:8080"}, allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class DanhGiaController {

    private final DanhGiaService danhGiaService;

    /**
     * GET /api/danh-gia/san-pham/{maSP}
     * Trả về toàn bộ đánh giá của sản phẩm (chỉ người đã mua)
     */
    @GetMapping("/san-pham/{maSP}")
    public ResponseEntity<ApiResponse<List<DanhGiaDTO>>> getDanhGia(
            @PathVariable Integer maSP) {
        try {
            List<DanhGiaDTO> list = danhGiaService.layDanhGia(maSP);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            log.error("Loi lay danh gia maSP={}: {}", maSP, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Khong the tai danh gia"));
        }
    }

    /**
     * GET /api/danh-gia/san-pham/{maSP}/thong-ke
     * Thống kê số lượng từng mức sao: { "1":0, "2":1, "3":2, "4":5, "5":10 }
     */
    @GetMapping("/san-pham/{maSP}/thong-ke")
    public ResponseEntity<ApiResponse<Map<Integer, Long>>> getThongKe(
            @PathVariable Integer maSP) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(danhGiaService.thongKeSao(maSP)));
        } catch (Exception e) {
            log.error("Loi thong ke sao maSP={}: {}", maSP, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Khong the tai thong ke"));
        }
    }
}