package poly.edu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.*;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.*;
import poly.edu.service.AuthService;
import poly.edu.service.EmailService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/orders")
public class QLHoaDonController {

    @Autowired
    private HoaDonDAO hoaDonDAO;
    
    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;
    
    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;
    
    @Autowired
    private QuanTriDAO quanTriDAO;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private EmailService emailService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy tất cả đơn hàng phân loại theo trạng thái
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        try {
            List<HoaDon> allHoaDons = hoaDonDAO.findAll();
            
            Map<String, List<Map<String, Object>>> allOrders = new HashMap<>();
            allOrders.put("dangxuly", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Đang xử lý")));
            allOrders.put("danggiao", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Đang giao")));
            allOrders.put("datuchoi", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Đã từ chối")));
            allOrders.put("hoantat", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Hoàn tất")));
            allOrders.put("baoloi", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Báo lỗi")));
            allOrders.put("hoanhang", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Hoàn hàng/trả hàng")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", allOrders);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy tất cả đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy đơn hàng theo trạng thái cụ thể
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(
            @RequestParam(value = "status", defaultValue = "Đang xử lý") String status) {
        try {
            List<HoaDon> hoaDons = hoaDonDAO.findByTrangThaiOrderByNgayMuaDesc(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", mapHoaDonToResponse(hoaDons));
            response.put("total", hoaDons.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable("id") Integer id) {
        try {
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            Map<String, Object> orderDetail = buildOrderDetail(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", orderDetail
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Duyệt đơn hàng - Chuyển từ "Đang xử lý" -> "Đang giao" và TRỪ KHO
     */
    @PostMapping("/{id}/approve")
    @Transactional
    public ResponseEntity<Map<String, Object>> approveOrder(@PathVariable("id") Integer id) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null || currentUser.getQuanTri() == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Không có quyền thực hiện thao tác này"
                ));
            }
            
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            // Chỉ duyệt được đơn ở trạng thái "Đang xử lý"
            if (!"Đang xử lý".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể duyệt đơn hàng ở trạng thái 'Đang xử lý'"
                ));
            }
            
            // Kiểm tra tồn kho trước khi duyệt
            List<String> outOfStockItems = new ArrayList<>();
            for (HoaDonCT chiTiet : hoaDon.getHoaDonCTs()) {
                SanPhamChiTiet sp = chiTiet.getSanPhamChiTiet();
                if (sp.getSoLuong() < chiTiet.getSoLuong()) {
                    outOfStockItems.add(sp.getSanPham().getTenSP() + " (" + sp.getTenMau() + 
                                       ", Size " + sp.getSize().getCoGiay() + ")");
                }
            }
            
            if (!outOfStockItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Sản phẩm không đủ số lượng: " + String.join(", ", outOfStockItems)
                ));
            }
            
            // TRỪ số lượng trong kho
            for (HoaDonCT chiTiet : hoaDon.getHoaDonCTs()) {
                sanPhamChiTietDAO.truSoLuong(
                    chiTiet.getSanPhamChiTiet().getMaSKU(), 
                    chiTiet.getSoLuong()
                );
            }
            
            // Cập nhật trạng thái và nhân viên duyệt
            hoaDon.setQuanTri(currentUser.getQuanTri());
            hoaDon.setTrangThai("Đang giao");
            hoaDonDAO.save(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã duyệt đơn hàng và trừ số lượng trong kho"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi duyệt đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Từ chối đơn hàng - Chuyển từ bất kỳ (trừ Hoàn tất) -> "Đã từ chối" KHÔNG ẢNH HƯỞNG KHO
     */
    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<Map<String, Object>> rejectOrder(@PathVariable("id") Integer id) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null || currentUser.getQuanTri() == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Không có quyền thực hiện thao tác này"
                ));
            }
            
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            String currentStatus = hoaDon.getTrangThai();
            
            // Không thể từ chối đơn đã hoàn tất
            if ("Hoàn tất".equals(currentStatus)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể từ chối đơn hàng đã hoàn tất"
                ));
            }
            
            // Nếu đơn đang ở trạng thái "Đang giao" (đã trừ kho) thì cần CỘNG lại kho
            if ("Đang giao".equals(currentStatus)) {
                for (HoaDonCT chiTiet : hoaDon.getHoaDonCTs()) {
                    sanPhamChiTietDAO.congSoLuong(
                        chiTiet.getSanPhamChiTiet().getMaSKU(), 
                        chiTiet.getSoLuong()
                    );
                }
            }
            
            hoaDon.setQuanTri(currentUser.getQuanTri());
            hoaDon.setTrangThai("Đã từ chối");
            hoaDonDAO.save(hoaDon);
            
            String message = "Đã từ chối đơn hàng";
            if ("Đang giao".equals(currentStatus)) {
                message += " và hoàn trả số lượng sản phẩm về kho";
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi từ chối đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Giao hàng thất bại - Từ "Đang giao" -> "Đã từ chối" và HOÀN KHO
     */
    @PostMapping("/{id}/delivery-failed")
    @Transactional
    public ResponseEntity<Map<String, Object>> deliveryFailed(@PathVariable("id") Integer id) {
        try {
            Users currentUser = authService.getCurrentUser();
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (!"Đang giao".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể đánh dấu giao thất bại cho đơn hàng đang giao"
                ));
            }
            
            // HOÀN KHO
            for (HoaDonCT chiTiet : hoaDon.getHoaDonCTs()) {
                sanPhamChiTietDAO.congSoLuong(
                    chiTiet.getSanPhamChiTiet().getMaSKU(), 
                    chiTiet.getSoLuong()
                );
            }
            
            hoaDon.setTrangThai("Đã từ chối");
            if (currentUser != null && currentUser.getQuanTri() != null) {
                hoaDon.setQuanTri(currentUser.getQuanTri());
            }
            hoaDonDAO.save(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật giao hàng thất bại và hoàn trả số lượng về kho"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Giao hàng thành công - Từ "Đang giao" -> "Hoàn tất" (và bắt đầu đếm thời gian)
     */
    @PostMapping("/{id}/delivery-success")
    @Transactional
    public ResponseEntity<Map<String, Object>> deliverySuccess(@PathVariable("id") Integer id) {
        try {
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (!"Đang giao".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể đánh dấu giao thành công cho đơn hàng đang giao"
                ));
            }
            
            hoaDon.setTrangThai("Hoàn tất");
            hoaDonDAO.save(hoaDon);
            
            // Gửi email thông báo cho khách hàng (tùy chọn)
            try {
                KhachHang kh = hoaDon.getKhachHang();
                if (kh != null && kh.getUser() != null && kh.getUser().getMail() != null) {
                    sendDeliverySuccessEmail(kh.getUser().getMail(), kh.getTenKH(), hoaDon.getMaHD());
                }
            } catch (Exception e) {
                // Log lỗi nhưng không ảnh hưởng
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật giao hàng thành công. Khách hàng có 1 phút để báo lỗi nếu có."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Khách hàng báo lỗi - Từ "Hoàn tất" (trong thời gian 1 phút) -> "Báo lỗi"
     */
    @PostMapping("/{id}/report-issue")
    @Transactional
    public ResponseEntity<Map<String, Object>> reportIssue(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (!"Hoàn tất".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể báo lỗi cho đơn hàng đã hoàn tất"
                ));
            }
            
            // Kiểm tra thời gian (1 phút)
            Date now = new Date();
            long diffInMillis = now.getTime() - hoaDon.getNgayMua().getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            
            if (diffInMinutes > 1) { // Đã quá 1 phút
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đã quá thời gian cho phép để báo lỗi (1 phút)"
                ));
            }
            
            String ghiChu = payload.getOrDefault("ghiChu", "");
            hoaDon.setTrangThai("Báo lỗi");
            hoaDon.setGhiChu(ghiChu);
            hoaDonDAO.save(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã ghi nhận báo lỗi. Nhân viên sẽ xử lý và liên hệ với bạn."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Khách hàng yêu cầu hoàn hàng - Từ "Hoàn tất" (trong thời gian 1 phút) -> "Hoàn hàng/trả hàng"
     */
    @PostMapping("/{id}/request-return")
    @Transactional
    public ResponseEntity<Map<String, Object>> requestReturn(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (!"Hoàn tất".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể yêu cầu hoàn hàng cho đơn hàng đã hoàn tất"
                ));
            }
            
            // Kiểm tra thời gian (1 phút)
            Date now = new Date();
            long diffInMillis = now.getTime() - hoaDon.getNgayMua().getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            
            if (diffInMinutes > 1) { // Đã quá 1 phút
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đã quá thời gian cho phép để yêu cầu hoàn hàng (1 phút)"
                ));
            }
            
            String lyDo = payload.getOrDefault("lyDo", "");
            hoaDon.setTrangThai("Hoàn hàng/trả hàng");
            hoaDon.setGhiChu("Yêu cầu hoàn hàng: " + lyDo);
            hoaDonDAO.save(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã ghi nhận yêu cầu hoàn hàng. Nhân viên sẽ xử lý và liên hệ với bạn."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Nhân viên xác nhận đã hoàn tất xử lý hoàn hàng - CỘNG LẠI KHO
     */
    @PostMapping("/{id}/confirm-return")
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmReturn(@PathVariable("id") Integer id) {
        try {
            Users currentUser = authService.getCurrentUser();
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (!"Hoàn hàng/trả hàng".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể xác nhận hoàn hàng cho đơn hàng đang chờ hoàn"
                ));
            }
            
            // CỘNG LẠI KHO
            for (HoaDonCT chiTiet : hoaDon.getHoaDonCTs()) {
                sanPhamChiTietDAO.congSoLuong(
                    chiTiet.getSanPhamChiTiet().getMaSKU(), 
                    chiTiet.getSoLuong()
                );
            }
            
            hoaDon.setGhiChu((hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() + " | " : "") + 
                             "Đã hoàn tất xử lý hoàn hàng và nhập lại kho");
            hoaDonDAO.save(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xác nhận hoàn hàng và cộng lại số lượng vào kho"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Nhân viên gửi email xin lỗi cho đơn hàng báo lỗi
     */
    @PostMapping("/{id}/send-apology-email")
    @Transactional
    public ResponseEntity<Map<String, Object>> sendApologyEmail(@PathVariable("id") Integer id) {
        try {
            HoaDon hoaDon = hoaDonDAO.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (!"Báo lỗi".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ có thể gửi email xin lỗi cho đơn hàng báo lỗi"
                ));
            }
            
            KhachHang kh = hoaDon.getKhachHang();
            if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy email khách hàng"
                ));
            }
            
            // Gửi email xin lỗi
            sendApologyEmailToCustomer(kh.getUser().getMail(), kh.getTenKH(), hoaDon);
            
            hoaDon.setGhiChu((hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() + " | " : "") + 
                             "Đã gửi email xin lỗi đến khách hàng");
            hoaDonDAO.save(hoaDon);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã gửi email xin lỗi đến khách hàng"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * JOB tự động ẩn nút báo lỗi sau 1 phút (chạy mỗi phút)
     * //ĐẾM 3 NGÀY - Chỉ cần đổi 60000 thành 3*24*60*60*1000 khi chuyển sang 3 ngày
     */
    @Scheduled(fixedRate = 60000) // 1 phút
    @Transactional
    public void autoExpireReportButtons() {
        try {
            // Lấy các đơn hàng "Hoàn tất" đã quá 1 phút
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -1); // 1 phút trước
            // ĐỔI THÀNH: cal.add(Calendar.DAY_OF_MONTH, -3); khi chuyển sang 3 ngày
            
            List<HoaDon> expiredOrders = hoaDonDAO.findByTrangThaiAndNgayMuaBefore(
                "Hoàn tất", cal.getTime()
            );
            
            // Không cần làm gì, chỉ cần log hoặc cập nhật flag nếu cần
            // Thực tế, frontend sẽ tự kiểm tra thời gian để ẩn nút
            System.out.println("Đã kiểm tra " + expiredOrders.size() + " đơn hàng hết hạn báo lỗi");
            
        } catch (Exception e) {
            System.err.println("Lỗi khi chạy job tự động: " + e.getMessage());
        }
    }

    // ==================== PRIVATE METHODS ====================

    private List<HoaDon> filterByTrangThai(List<HoaDon> list, String trangThai) {
        return list.stream()
                .filter(h -> trangThai.equals(h.getTrangThai()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapHoaDonToResponse(List<HoaDon> hoaDons) {
        return hoaDons.stream().map(this::buildOrderSummary).collect(Collectors.toList());
    }

    private Map<String, Object> buildOrderSummary(HoaDon hd) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("maHD", hd.getMaHD());
        orderMap.put("maHDStr", String.format("HD%04d", hd.getMaHD()));
        orderMap.put("ngayMua", hd.getNgayMua());
        orderMap.put("trangThai", hd.getTrangThai());
        orderMap.put("phuongThucTT", hd.getPhuongThucTT());
        orderMap.put("ghiChu", hd.getGhiChu());

        // Parse địa chỉ
        if (hd.getDiaChiJson() != null && !hd.getDiaChiJson().isEmpty()) {
            try {
                DiaChiJsonDTO diaChi = objectMapper.readValue(hd.getDiaChiJson(), DiaChiJsonDTO.class);
                orderMap.put("sdt", diaChi.getSdt());
                orderMap.put("tenNN", diaChi.getTenNN());
                orderMap.put("diemGiao", diaChi.getDiemGiao());
            } catch (Exception e) {
                orderMap.put("sdt", "");
                orderMap.put("tenNN", "");
                orderMap.put("diemGiao", "");
            }
        } else {
            orderMap.put("sdt", "");
            orderMap.put("tenNN", "");
            orderMap.put("diemGiao", "");
        }

        // Thông tin nhân viên
        if (hd.getQuanTri() != null) {
            orderMap.put("maQT", hd.getQuanTri().getMaQT());
            orderMap.put("maQTStr", String.format("QT%04d", hd.getQuanTri().getMaQT()));
            orderMap.put("tenQT", hd.getQuanTri().getTenQT());
            
            if (hd.getQuanTri().getUser() != null) {
                orderMap.put("emailQT", hd.getQuanTri().getUser().getMail());
            }
        }

        // Tính tổng tiền
        double tongTien = 0.0;
        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            tongTien = hd.getHoaDonCTs().stream()
                    .mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia())
                    .sum();
        }
        orderMap.put("tongTien", tongTien);

        return orderMap;
    }

    private Map<String, Object> buildOrderDetail(HoaDon hd) {
        Map<String, Object> detail = buildOrderSummary(hd);
        
        // Thông tin khách hàng
        if (hd.getKhachHang() != null) {
            detail.put("maKH", hd.getKhachHang().getMaKH());
            detail.put("tenKH", hd.getKhachHang().getTenKH());
            detail.put("sdtKH", hd.getKhachHang().getSdt());
        }

        // Chi tiết sản phẩm
        List<Map<String, Object>> chiTietList = new ArrayList<>();
        double tongTien = 0.0;
        
        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                Map<String, Object> ctMap = new HashMap<>();
                ctMap.put("maSKU", ct.getSanPhamChiTiet().getMaSKU());
                
                if (ct.getSanPhamChiTiet().getSanPham() != null) {
                    ctMap.put("tenSP", ct.getSanPhamChiTiet().getSanPham().getTenSP());
                }
                
                ctMap.put("tenMau", ct.getSanPhamChiTiet().getTenMau());
                
                if (ct.getSanPhamChiTiet().getSize() != null) {
                    ctMap.put("size", ct.getSanPhamChiTiet().getSize().getCoGiay());
                }
                
                ctMap.put("hinhAnh", ct.getSanPhamChiTiet().getHinhAnh());
                ctMap.put("soLuong", ct.getSoLuong());
                ctMap.put("donGia", ct.getDonGia());
                ctMap.put("thanhTien", ct.getSoLuong() * ct.getDonGia());
                
                tongTien += ct.getSoLuong() * ct.getDonGia();
                chiTietList.add(ctMap);
            }
        }
        
        detail.put("chiTiet", chiTietList);
        detail.put("tongTien", tongTien);
        
        return detail;
    }

    private void sendDeliverySuccessEmail(String email, String tenKH, Integer maHD) {
        try {
	    	String subject = "SHOEDO SHOP - Đơn hàng đã giao thành công";
	        String htmlContent = "<!DOCTYPE html>"
	                + "<html><head><meta charset='UTF-8'>"
	                + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{padding:20px}.warning{background:#fff3cd;padding:10px;border-radius:5px;margin:15px 0}</style>"
	                + "</head><body>"
	                + "<div class='container'>"
	                + "<div class='header'><h2>ShoeDo Shop - Giao hàng thành công</h2></div>"
	                + "<div class='content'>"
	                + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
	                + "<p>Đơn hàng #HD" + String.format("%04d", maHD) + " của bạn đã được giao thành công.</p>"
	                + "<div class='warning'>"
	                + "<p><strong>Lưu ý:</strong> Bạn có 1 phút để kiểm tra đơn hàng và báo lỗi (nếu có).</p>"
	                + "<p>• Nếu có vấn đề: Bấm <strong>'Báo lỗi'</strong> hoặc <strong>'Hoàn hàng'</strong> trong vòng 1 phút</p>"
	                + "<p>• Sau 1 phút, đơn hàng sẽ được xác nhận hoàn tất và không thể thay đổi</p>"
	                + "</div>"
	                + "<p>Cảm ơn bạn đã mua sắm tại ShoeDo Shop!</p>"
	                + "</div></div></body></html>";
	        
	        emailService.sendHtmlEmail(email, subject, htmlContent);
	    } catch (Exception e) {
	        // Log lỗi nhưng không throw ra ngoài
	        System.err.println("Lỗi gửi email: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

    private void sendApologyEmailToCustomer(String email, String tenKH, HoaDon hoaDon) {
    	try {
	    	String subject = "SHOEDO SHOP - Xin lỗi về sự cố đơn hàng #HD" + String.format("%04d", hoaDon.getMaHD());
	        String htmlContent = "<!DOCTYPE html>"
	                + "<html><head><meta charset='UTF-8'>"
	                + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{padding:20px}.apology{background:#f8d7da;color:#721c24;padding:15px;border-radius:5px;margin:15px 0}</style>"
	                + "</head><body>"
	                + "<div class='container'>"
	                + "<div class='header'><h2>ShoeDo Shop - Xin lỗi quý khách</h2></div>"
	                + "<div class='content'>"
	                + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
	                + "<div class='apology'>"
	                + "<h3>Chúng tôi xin lỗi về sự cố đơn hàng</h3>"
	                + "<p>Đơn hàng #HD" + String.format("%04d", hoaDon.getMaHD()) + " của bạn đã gặp sự cố.</p>"
	                + "<p><strong>Nội dung lỗi:</strong> " + (hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "Không xác định") + "</p>"
	                + "</div>"
	                + "<p>Đội ngũ ShoeDo Shop đã xử lý sự cố này. Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ hotline của chúng tôi.</p>"
	                + "<p>Một lần nữa, chúng tôi xin lỗi về sự bất tiện này và hy vọng sẽ phục vụ bạn tốt hơn trong tương lai.</p>"
	                + "</div></div></body></html>";
	        
	        emailService.sendHtmlEmail(email, subject, htmlContent);
    	 } catch (Exception e) {
 	        // Log lỗi nhưng không throw ra ngoài
 	        System.err.println("Lỗi gửi email: " + e.getMessage());
 	        e.printStackTrace();
 	    }
 	}
}