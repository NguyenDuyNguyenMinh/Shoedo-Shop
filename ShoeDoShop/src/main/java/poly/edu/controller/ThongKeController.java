package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.ThongKeService;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/thongke")
@CrossOrigin(origins = "*")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    /**
     * Thống kê mặc định - 30 ngày gần nhất
     * GET /api/employee/thongke
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getThongKeMacDinh() {
        return ResponseEntity.ok(thongKeService.thongKeMacDinh());
    }

    /**
     * Thống kê tổng quan (Dashboard)
     * GET /api/employee/thongke/tong-quan?startDate=2026-01-01&endDate=2026-03-08
     */
    @GetMapping("/tong-quan")
    public ResponseEntity<Map<String, Object>> getTongQuan(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(thongKeService.thongKeTongQuan(startDate, endDate));
    }

    /**
     * Thống kê doanh thu theo ngày
     * GET /api/employee/thongke/ngay?startDate=2026-01-01&endDate=2026-03-08
     */
    @GetMapping("/ngay")
    public ResponseEntity<?> getThongKeTheoNgay(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(thongKeService.thongKeTheoNgay(startDate, endDate));
    }

    /**
     * Thống kê doanh thu theo tháng
     * GET /api/employee/thongke/thang?startDate=2025-01-01&endDate=2026-12-31
     */
    @GetMapping("/thang")
    public ResponseEntity<?> getThongKeTheoThang(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(thongKeService.thongKeTheoThang(startDate, endDate));
    }

    /**
     * Thống kê doanh thu theo năm
     * GET /api/employee/thongke/nam?startDate=2024-01-01&endDate=2026-12-31
     */
    @GetMapping("/nam")
    public ResponseEntity<?> getThongKeTheoNam(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(thongKeService.thongKeTheoNam(startDate, endDate));
    }

    /**
     * Thống kê theo danh mục sản phẩm
     * GET /api/employee/thongke/danh-muc?startDate=2026-01-01&endDate=2026-03-08
     */
    @GetMapping("/danh-muc")
    public ResponseEntity<?> getThongKeTheoDanhMuc(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(thongKeService.thongKeTheoDanhMuc(startDate, endDate));
    }

    /**
     * Top sản phẩm bán chạy
     * GET /api/employee/thongke/top-san-pham?startDate=2026-01-01&endDate=2026-03-08&limit=10
     */
    @GetMapping("/top-san-pham")
    public ResponseEntity<?> getTopSanPham(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(thongKeService.topSanPhamBanChay(startDate, endDate, limit));
    }

    /**
     * Top khách hàng
     * GET /api/employee/thongke/top-khach-hang?startDate=2026-01-01&endDate=2026-03-08&limit=5
     */
    @GetMapping("/top-khach-hang")
    public ResponseEntity<?> getTopKhachHang(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(thongKeService.topKhachHang(startDate, endDate, limit));
    }

    /**
     * Thống kê theo trạng thái đơn hàng
     * GET /api/employee/thongke/trang-thai?startDate=2026-01-01&endDate=2026-03-08
     */
    @GetMapping("/trang-thai")
    public ResponseEntity<?> getThongKeTheoTrangThai(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return ResponseEntity.ok(thongKeService.thongKeTheoTrangThai(startDate, endDate));
    }
}
