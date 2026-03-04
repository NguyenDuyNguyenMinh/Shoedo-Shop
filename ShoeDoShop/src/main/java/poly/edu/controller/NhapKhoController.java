
package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.NhapKhoDTO;
import poly.edu.entity.NhapKho;
import poly.edu.entity.SanPhamChiTiet;
import poly.edu.service.NhapKhoService;

import java.util.List;


@RestController
@RequestMapping("/api/nhapkho")
public class NhapKhoController {

    @Autowired
    private NhapKhoService nhapKhoService;
    @Autowired
    private poly.edu.dao.DanhMucDAO danhMucDAO;

    @GetMapping("/sanpham")
    public ResponseEntity<List<SanPhamChiTiet>> getSanPhamForImport() {
        return ResponseEntity.ok(nhapKhoService.getAllSanPhamChiTiet());
    }

    @GetMapping("/lichsu")
    public ResponseEntity<List<NhapKho>> getLichSu() {
        return ResponseEntity.ok(nhapKhoService.getLichSuNhapKho());
    }

    @PostMapping("/nhap")
    public ResponseEntity<?> nhapKhoDondGiam(@RequestBody NhapKhoDTO request) {
        try {
            NhapKho result = nhapKhoService.thucHienNhapKho(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
 // THÊM API NÀY: Nhận mảng dữ liệu từ Vue.js
    @PostMapping("/nhap-hang-loat")
    public ResponseEntity<?> nhapKhoHangLoat(@RequestBody List<NhapKhoDTO> requests) {
        try {
            List<NhapKho> result = nhapKhoService.thucHienNhapKhoHangLoat(requests);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/danhmuc")
    public ResponseEntity<?> getAllDanhMuc() {
        return ResponseEntity.ok(danhMucDAO.findAll());
    }
}