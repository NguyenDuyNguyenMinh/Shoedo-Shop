package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.SanPhamService;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.dto.SanPhamNDTO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sanpham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;
    @Autowired
    private poly.edu.dao.SizeDAO sizeDAO;
    @Autowired
    private poly.edu.dao.DanhMucDAO danhMucDAO;

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

    @Value("${file.upload-dir}")
    private String uploadDir;

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Integer id) {
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
}