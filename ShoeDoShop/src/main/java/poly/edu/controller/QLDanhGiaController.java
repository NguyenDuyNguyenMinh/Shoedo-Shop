package poly.edu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.DanhGia;
import poly.edu.service.QLDanhGiaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/danhgia")
@Slf4j
public class QLDanhGiaController {

    @Autowired
    private QLDanhGiaService danhGiaService;

    @GetMapping("/list")
    public ResponseEntity<?> getAllDanhGia() {
        try {
            List<DanhGia> danhGiaList = danhGiaService.getAllDanhGia();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", danhGiaList
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Không thể tải danh sách đánh giá: " + e.getMessage(),
                "data", List.of()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDanhGiaById(@PathVariable Integer id) {
        try {
            Optional<DanhGia> danhGia = danhGiaService.getDanhGiaById(id);
            if (danhGia.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", danhGia.get()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đánh giá"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Không thể tải chi tiết đánh giá: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDanhGia(@PathVariable Integer id) {
        try {
            boolean deleted = danhGiaService.deleteDanhGia(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa đánh giá thành công"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đánh giá cần xóa"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Không thể xóa đánh giá: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/filter/sao/{sao}")
    public ResponseEntity<?> filterBySao(@PathVariable Integer sao) {
        try {
            if (sao < 1 || sao > 5) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Số sao phải từ 1 đến 5"
                ));
            }
            List<DanhGia> danhGiaList = danhGiaService.getDanhGiaBySao(sao);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", danhGiaList
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Không thể lọc đánh giá: " + e.getMessage()
            ));
        }
    }
}