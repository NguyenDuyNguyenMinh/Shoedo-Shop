package poly.edu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.dto.ApiResponse;
import poly.edu.dto.SanPhamDTO;
import poly.edu.dto.SanPhamDetailDTO;
import poly.edu.dto.SanPhamLienQuanDTO;
import poly.edu.dto.SanPhamNDTO;
import poly.edu.service.SanPhamDetailService;
import poly.edu.service.SanPhamService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/san-pham")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:8080"}, allowCredentials = "true")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private SanPhamDetailService sanPhamDetailService;

    @Autowired
    private poly.edu.dao.SizeDAO sizeDAO;

    @Autowired
    private poly.edu.dao.DanhMucDAO danhMucDAO;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ══════════════════════════════════════════════════════════════
    //  QUẢN LÝ SẢN PHẨM (Admin)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/list")
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(sanPhamService.getAllProductList());
    }

    @GetMapping("/danhmuc")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(danhMucDAO.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody SanPhamNDTO dto) {
        return ResponseEntity.ok(sanPhamService.createProduct(dto));
    }

    @GetMapping("/images")
    public ResponseEntity<List<String>> getAllImages() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String[] files = dir.list((current, name) -> new File(current, name).isFile());
        return ResponseEntity.ok(files != null ? Arrays.asList(files) : new ArrayList<>());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File trống"));
            }
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + File.separator + fileName);
            Files.write(path, file.getBytes());
            return ResponseEntity.ok(Map.of("success", true, "fileName", fileName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getProductDetailAdmin(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(sanPhamService.getProductDetail(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProduct(@RequestBody SanPhamNDTO dto) {
        try {
            return ResponseEntity.ok(sanPhamService.updateProduct(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/sizes")
    public ResponseEntity<?> getAllSizes() {
        return ResponseEntity.ok(sizeDAO.findAll());
    }

    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleProductStatus(@PathVariable Integer id) {
        try {
            boolean newStatus = sanPhamService.toggleProductStatus(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isActive", newStatus,
                "message", "Cập nhật trạng thái thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/danhmuc/create")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> payload) {
        try {
            String tenDM = payload.get("tenDM");
            if (tenDM == null || tenDM.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên danh mục không được để trống"));
            }
            poly.edu.entity.DanhMuc newCategory = new poly.edu.entity.DanhMuc();
            newCategory.setTenDM(tenDM);
            danhMucDAO.save(newCategory);
            return ResponseEntity.ok(Map.of("success", true, "data", newCategory));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  TRANG CHỦ (Public)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<List<SanPhamDTO>>> getFlashSales() {
        log.info("GET /api/san-pham/flash-sales");
        try {
            return ResponseEntity.ok(ApiResponse.ok(sanPhamService.layFlashSales()));
        } catch (Exception e) {
            log.error("Lỗi getFlashSales: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Không thể tải Flash Sales"));
        }
    }

    @GetMapping("/noi-bat")
    public ResponseEntity<ApiResponse<List<SanPhamDTO>>> getNoiBat() {
        log.info("GET /api/san-pham/noi-bat");
        try {
            return ResponseEntity.ok(ApiResponse.ok(sanPhamService.layNoiBat()));
        } catch (Exception e) {
            log.error("Lỗi getNoiBat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Không thể tải sản phẩm nổi bật"));
        }
    }

    @GetMapping("/ban-chay")
    public ResponseEntity<ApiResponse<List<SanPhamDTO>>> getBanChay() {
        log.info("GET /api/san-pham/ban-chay");
        try {
            return ResponseEntity.ok(ApiResponse.ok(sanPhamService.layBanChay()));
        } catch (Exception e) {
            log.error("Lỗi getBanChay: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Không thể tải sản phẩm bán chạy"));
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
            return ResponseEntity.internalServerError().body(ApiResponse.error("Không thể tải dữ liệu trang chủ"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  TRANG CHI TIẾT (Public)
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SanPhamDetailDTO>> getChiTiet(@PathVariable Integer id) {
        log.info("GET /api/san-pham/{}", id);
        try {
            SanPhamDetailDTO dto = sanPhamDetailService.layChiTiet(id);
            return ResponseEntity.ok(ApiResponse.ok(dto));
        } catch (NoSuchElementException e) {
            log.warn("Không tìm thấy sản phẩm id={}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi getChiTiet id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Không thể tải chi tiết sản phẩm"));
        }
    }

    @GetMapping("/{id}/lien-quan")
    public ResponseEntity<ApiResponse<List<SanPhamLienQuanDTO>>> getLienQuan(@PathVariable Integer id) {
        log.info("GET /api/san-pham/{}/lien-quan", id);
        try {
            List<SanPhamLienQuanDTO> list = sanPhamDetailService.layLienQuan(id);
            return ResponseEntity.ok(ApiResponse.ok(list));
        } catch (Exception e) {
            log.error("Lỗi getLienQuan id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Không thể tải sản phẩm liên quan"));
        }
    }

    public record TrangChuResponse(
            List<SanPhamDTO> flashSales,
            List<SanPhamDTO> noiBat,
            List<SanPhamDTO> banChay
    ) {}
}
