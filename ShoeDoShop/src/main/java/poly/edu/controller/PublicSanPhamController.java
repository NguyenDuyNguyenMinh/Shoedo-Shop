package poly.edu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.ApiResponse;
import poly.edu.dto.SanPhamListDTO;
import poly.edu.service.PublicSanPhamService;

import java.util.List;

/**
 * REST Controller phục vụ trang KH_SanPham (Vue).
 *
 * Base URL: /api/public
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  TRANG KH_SANPHAM                                                    │
 * │  GET /api/public/products          → Danh sách SP (filter + sort)   │
 * │  GET /api/public/search            → Tìm kiếm theo tên              │
 * │  GET /api/public/products/{id}     → 1 SP (dùng cho DetailProduct)  │
 * │  GET /api/categories               → Danh sách tên danh mục         │
 * │                                                                      │
 * │  sort values:                                                        │
 * │    default    → mặc định (theo maSP tăng dần)                       │
 * │    price_asc  → giá tăng dần                                        │
 * │    price_desc → giá giảm dần                                        │
 * │    newest     → mới nhất (maSP giảm dần)                            │
 * │    flash_sale → chỉ SP có KM > 0, sort % KM giảm dần               │
 * │    ban_chay   → sort theo số lượng đã bán giảm dần                  │
 * └──────────────────────────────────────────────────────────────────────┘
 *
 * Query params cho /api/public/products:
 *   category : tên danh mục (optional)           — vd: "Giày da"
 *   gender   : "Nam" | "Nữ" | "Tất cả" (optional)
 *   inStock  : true/false (default: false)
 *   sort     : "default" | "price_asc" | "price_desc" | "newest" | "flash_sale" | "ban_chay"
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:8080"}, allowCredentials = "true")
public class PublicSanPhamController {

    private final PublicSanPhamService publicSanPhamService;

    // ──────────────────────────────────────────────────────────────
    //  GET /api/public/products
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/api/public/products")
    public ResponseEntity<ApiResponse<List<SanPhamListDTO>>> getProducts(
            @RequestParam(required = false)                          String  category,
            @RequestParam(required = false)                          String  gender,
            @RequestParam(required = false, defaultValue = "false")  boolean inStock,
            @RequestParam(required = false, defaultValue = "default") String  sort
    ) {
        log.info("GET /api/public/products category={} gender={} inStock={} sort={}",
                category, gender, inStock, sort);
        try {
            List<SanPhamListDTO> data = publicSanPhamService.laySanPhamList(
                    category, gender, inStock, sort);
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            log.error("Lỗi getProducts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải danh sách sản phẩm"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  GET /api/public/search?keyword=...
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/api/public/search")
    public ResponseEntity<ApiResponse<List<SanPhamListDTO>>> searchProducts(
            @RequestParam(required = false) String keyword
    ) {
        log.info("GET /api/public/search keyword={}", keyword);
        try {
            List<SanPhamListDTO> data = publicSanPhamService.timKiem(keyword);
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            log.error("Lỗi searchProducts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tìm kiếm sản phẩm"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  GET /api/public/products/{id}
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/api/public/products/{id}")
    public ResponseEntity<ApiResponse<SanPhamListDTO>> getProductById(
            @PathVariable Integer id
    ) {
        log.info("GET /api/public/products/{}", id);
        try {
            List<SanPhamListDTO> all = publicSanPhamService.laySanPhamList(
                    null, null, false, "default");

            return all.stream()
                    .filter(p -> p.getMaSP().equals(id))
                    .findFirst()
                    .<ResponseEntity<ApiResponse<SanPhamListDTO>>>map(
                            p -> ResponseEntity.ok(ApiResponse.ok(p)))
                    .orElse(ResponseEntity.status(404)
                            .body(ApiResponse.error("Không tìm thấy sản phẩm")));

        } catch (Exception e) {
            log.error("Lỗi getProductById id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải sản phẩm"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  GET /api/categories
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/api/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        log.info("GET /api/categories");
        try {
            List<String> data = publicSanPhamService.layDanhSachDanhMuc();
            return ResponseEntity.ok(ApiResponse.ok(data));
        } catch (Exception e) {
            log.error("Lỗi getCategories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Không thể tải danh mục"));
        }
    }
}