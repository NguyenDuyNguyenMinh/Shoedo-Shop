package poly.edu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.ApiResponse;
import poly.edu.dto.SanPhamDTO;
import poly.edu.dto.SanPhamDetailDTO;
import poly.edu.dto.SanPhamLienQuanDTO;
import poly.edu.service.SanPhamDetailService;
import poly.edu.service.SanPhamService;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST Controller cho toàn bộ API sản phẩm của ShoedoShop.
 *
 * Base URL: /api/san-pham
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  TRANG CHỦ                                                      │
 * │  GET /api/san-pham/flash-sales    → 5 SP khuyến mãi cao nhất   │
 * │  GET /api/san-pham/noi-bat        → 5 SP mới nhất              │
 * │  GET /api/san-pham/ban-chay       → 5 SP bán chạy nhất         │
 * │  GET /api/san-pham/trang-chu      → Cả 3 section 1 lần gọi     │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  TRANG CHI TIẾT                                                 │
 * │  GET /api/san-pham/{id}           → Chi tiết sản phẩm          │
 * │  GET /api/san-pham/{id}/lien-quan → Sản phẩm cùng danh mục     │
 * └─────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SanPhamController {

    private final SanPhamService       sanPhamService;
    private final SanPhamDetailService sanPhamDetailService;

    // ══════════════════════════════════════════════════════════════
    //  TRANG CHỦ
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<List<SanPhamDTO>>> getFlashSales() {
        log.info("GET /api/san-pham/flash-sales");
        try {
            return ResponseEntity.ok(ApiResponse.ok(sanPhamService.layFlashSales()));
        } catch (Exception e) {
            log.error("Lỗi getFlashSales: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải Flash Sales"));
        }
    }

    @GetMapping("/noi-bat")
    public ResponseEntity<ApiResponse<List<SanPhamDTO>>> getNoiBat() {
        log.info("GET /api/san-pham/noi-bat");
        try {
            return ResponseEntity.ok(ApiResponse.ok(sanPhamService.layNoiBat()));
        } catch (Exception e) {
            log.error("Lỗi getNoiBat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải sản phẩm nổi bật"));
        }
    }

    @GetMapping("/ban-chay")
    public ResponseEntity<ApiResponse<List<SanPhamDTO>>> getBanChay() {
        log.info("GET /api/san-pham/ban-chay");
        try {
            return ResponseEntity.ok(ApiResponse.ok(sanPhamService.layBanChay()));
        } catch (Exception e) {
            log.error("Lỗi getBanChay: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải sản phẩm bán chạy"));
        }
    }

    @GetMapping("/trang-chu")
    public ResponseEntity<ApiResponse<TrangChuResponse>> getTrangChu() {
        log.info("GET /api/san-pham/trang-chu");
        try {
            TrangChuResponse body = new TrangChuResponse(
                    sanPhamService.layFlashSales(),
                    sanPhamService.layNoiBat(),
                    sanPhamService.layBanChay()
            );
            return ResponseEntity.ok(ApiResponse.ok(body));
        } catch (Exception e) {
            log.error("Lỗi getTrangChu: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải dữ liệu trang chủ"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  TRANG CHI TIẾT
    // ══════════════════════════════════════════════════════════════

    /**
     * GET /api/san-pham/{id}
     * Trả về toàn bộ thông tin sản phẩm: tên, mô tả, giá, màu, size,
     * hình ảnh, danh mục, từng SKU còn hàng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanPhamDetailDTO>> getChiTiet(
            @PathVariable Integer id) {
        log.info("GET /api/san-pham/{}", id);
        try {
            SanPhamDetailDTO dto = sanPhamDetailService.layChiTiet(id);
            return ResponseEntity.ok(ApiResponse.ok(dto));
        } catch (NoSuchElementException e) {
            log.warn("Không tìm thấy sản phẩm id={}: {}", id, e.getMessage());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi getChiTiet id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải chi tiết sản phẩm"));
        }
    }

    /**
     * GET /api/san-pham/{id}/lien-quan
     * Trả về danh sách sản phẩm cùng danh mục (tối đa ~10 SP).
     */
    @GetMapping("/{id}/lien-quan")
    public ResponseEntity<ApiResponse<List<SanPhamLienQuanDTO>>> getLienQuan(
            @PathVariable Integer id) {
        log.info("GET /api/san-pham/{}/lien-quan", id);
        try {
            List<SanPhamLienQuanDTO> list = sanPhamDetailService.layLienQuan(id);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            log.error("Lỗi getLienQuan id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải sản phẩm liên quan"));
        }
    }

    // ── Record gộp response trang chủ ────────────────────────────
    public record TrangChuResponse(
            List<SanPhamDTO> flashSales,
            List<SanPhamDTO> noiBat,
            List<SanPhamDTO> banChay
    ) {}
}