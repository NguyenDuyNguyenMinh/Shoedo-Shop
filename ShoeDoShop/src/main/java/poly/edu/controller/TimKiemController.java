package poly.edu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.ApiResponse;
import poly.edu.dto.TimKiemDTO;
import poly.edu.service.TimKiemService;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý lịch sử tìm kiếm của khách hàng.
 *
 * BASE: /api/tim-kiem
 *
 * GET  /api/tim-kiem/lich-su/{maKH}          → Lấy lịch sử (tối đa 10)
 * POST /api/tim-kiem/luu                      → Lưu từ khóa { maKH, keyword }
 * DELETE /api/tim-kiem/xoa-tat-ca/{maKH}      → Xóa toàn bộ
 * DELETE /api/tim-kiem/xoa/{maKH}?keyword=... → Xóa 1 từ khóa
 */
@RestController
@RequestMapping("/api/tim-kiem")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:8080"}, allowCredentials = "true")
public class TimKiemController {

    private final TimKiemService timKiemService;

    // ── Lấy lịch sử tìm kiếm ────────────────────────────────────
    @GetMapping("/lich-su/{maKH}")
    public ResponseEntity<ApiResponse<List<TimKiemDTO>>> getLichSu(
            @PathVariable Integer maKH) {
        log.info("GET /api/tim-kiem/lich-su/{}", maKH);
        try {
            return ResponseEntity.ok(
                    ApiResponse.ok(timKiemService.layLichSu(maKH)));
        } catch (Exception e) {
            log.error("Lỗi getLichSu maKH={}: {}", maKH, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải lịch sử tìm kiếm"));
        }
    }

    // ── Lưu từ khóa sau khi tìm kiếm ────────────────────────────
    @PostMapping("/luu")
    public ResponseEntity<ApiResponse<Void>> luuTuKhoa(
            @RequestBody Map<String, Object> body) {
        try {
            Integer maKH    = (Integer) body.get("maKH");
            String  keyword = (String)  body.get("keyword");
            if (maKH == null || keyword == null || keyword.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Thiếu maKH hoặc keyword"));
            }
            timKiemService.luuTuKhoa(maKH, keyword);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (Exception e) {
            log.error("Lỗi luuTuKhoa: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể lưu từ khóa"));
        }
    }

    // ── Xóa toàn bộ lịch sử ─────────────────────────────────────
    @DeleteMapping("/xoa-tat-ca/{maKH}")
    public ResponseEntity<ApiResponse<Void>> xoaTatCa(
            @PathVariable Integer maKH) {
        log.info("DELETE /api/tim-kiem/xoa-tat-ca/{}", maKH);
        try {
            timKiemService.xoaTatCa(maKH);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (Exception e) {
            log.error("Lỗi xoaTatCa maKH={}: {}", maKH, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể xóa lịch sử"));
        }
    }

    // ── Xóa 1 từ khóa ───────────────────────────────────────────
    @DeleteMapping("/xoa/{maKH}")
    public ResponseEntity<ApiResponse<Void>> xoaTuKhoa(
            @PathVariable Integer maKH,
            @RequestParam String keyword) {
        log.info("DELETE /api/tim-kiem/xoa/{} keyword={}", maKH, keyword);
        try {
            timKiemService.xoaTuKhoa(maKH, keyword);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (Exception e) {
            log.error("Lỗi xoaTuKhoa maKH={}: {}", maKH, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể xóa từ khóa"));
        }
    }
}