package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.QLDanhGiaService;

import java.util.Map;

@RestController
@RequestMapping("/api/danhgia")
public class QLDanhGiaController {

    @Autowired private QLDanhGiaService danhGiaService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllDanhGia() {
        return ResponseEntity.ok(danhGiaService.getAllDanhGia());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDanhGiaById(@PathVariable Integer id) {
        return ResponseEntity.ok(danhGiaService.getDanhGiaById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteDanhGia(@PathVariable Integer id) {
        return ResponseEntity.ok(danhGiaService.deleteDanhGia(id));
    }
}