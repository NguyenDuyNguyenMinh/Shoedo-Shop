package poly.edu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import poly.edu.dao.*;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QLHoaDonService {

    @Autowired private HoaDonDAO hoaDonDAO;
    @Autowired private HoaDonCTDAO hoaDonCTDAO;
    @Autowired private SanPhamChiTietDAO spctDAO;
    @Autowired private AuthService authService;
    @Autowired private EmailService emailService;
    @Autowired private PdfService pdfService;
    
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== GET METHODS ====================
    public Map<String, Object> getAllOrders() {
    	List<HoaDon> all = hoaDonDAO.findAll();
  
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("pending", mapList(filterByStatus(all, "Đang xử lý")));
        result.put("delivering", mapList(filterByStatus(all, "Đang giao")));
        result.put("completed", mapList(filterByStatus(all, "Hoàn tất")));
        result.put("rejected", mapList(filterByStatus(all, "Đã từ chối")));
        result.put("error", mapList(filterByStatus(all, "Báo lỗi")));
        
        return success("data", result);
    }

    public Map<String, Object> getOrdersByStatus(String status) {
        List<HoaDon> list = hoaDonDAO.findByTrangThaiOrderByNgayMuaDesc(status);
        return success(Map.of("orders", mapList(list), "total", list.size()));
    }

    public Map<String, Object> getOrderDetail(Integer id) {
        HoaDon hd = findOrder(id);
        return success("order", buildDetail(hd));
    }

    // ==================== ORDER ACTIONS ====================
    @Transactional
    public Map<String, Object> confirmOrder(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang xử lý", "Chỉ có thể xác nhận đơn hàng ở trạng thái 'Đang xử lý'");
        checkEmployee();

        List<String> outOfStock = new ArrayList<>();
        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            if (ct.getSanPhamChiTiet().getSoLuong() < ct.getSoLuong()) {
                outOfStock.add(ct.getSanPhamChiTiet().getSanPham().getTenSP());
            }
        }
        if (!outOfStock.isEmpty()) {
            return error("Sản phẩm không đủ số lượng: " + String.join(", ", outOfStock));
        }

        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            spctDAO.truSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
        }

        hd.setQuanTri(getCurrentEmployee());
        hd.setTrangThai("Đang giao");
        hoaDonDAO.save(hd);

        return success("Đã xác nhận đơn hàng và trừ số lượng trong kho");
    }

    @Transactional
    public Map<String, Object> rejectOrder(Integer id, Map<String, String> payload) {
        HoaDon hd = findOrder(id);
        checkEmployee();

        String current = hd.getTrangThai();
        if ("Hoàn tất".equals(current)) {
            return error("Không thể từ chối đơn hàng đã hoàn tất");
        }

        String lyDo = payload.getOrDefault("lyDo", "Không có lý do");

        if ("Đang giao".equals(current)) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
            }
        }

        hd.setQuanTri(getCurrentEmployee());
        hd.setTrangThai("Đã từ chối");
        hd.setGhiChu(lyDo);
        hoaDonDAO.save(hd);

        String msg = "Đã từ chối đơn hàng";
        if ("Đang giao".equals(current)) msg += " và hoàn trả số lượng về kho";
        return success(msg);
    }

    @Transactional
    public Map<String, Object> deliveryFailed(Integer id, Map<String, String> payload) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang giao", "Chỉ có thể đánh dấu thất bại cho đơn hàng đang giao");

        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
        }

        hd.setTrangThai("Đã từ chối");
        hd.setGhiChu(payload.getOrDefault("lyDo", "Giao hàng thất bại"));
        hd.setQuanTri(getCurrentEmployee());
        hoaDonDAO.save(hd);

        return success("Đã cập nhật giao thất bại và hoàn trả số lượng về kho");
    }

    @Transactional
    public Map<String, Object> deliverySuccess(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang giao", "Chỉ có thể đánh dấu thành công cho đơn hàng đang giao");

        hd.setTrangThai("Hoàn tất");
        hd.setNgayDen(new Date());
        hd.setQuanTri(getCurrentEmployee());
        hoaDonDAO.save(hd);

        try {
            sendSuccessEmail(hd);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }

        return success("Đã cập nhật giao hàng thành công. KH có 3 ngày để báo lỗi/hoàn hàng");
    }

    @Transactional
    public Map<String, Object> sendApologyEmail(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Báo lỗi", "Chỉ có thể gửi email xin lỗi cho đơn hàng báo lỗi");

        KhachHang kh = hd.getKhachHang();
        if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) {
            return error("Không tìm thấy email khách hàng");
        }

        sendApologyEmailWithPdf(hd);
        hd.setTrangThai("Hoàn tất");
        hd.setQuanTri(getCurrentEmployee());
        hoaDonDAO.save(hd);

        return success("Đã gửi email xin lỗi kèm hóa đơn PDF");
    }

    public ResponseEntity<?> printInvoice(Integer id) {
        try {
            HoaDon hd = findOrder(id);
            byte[] pdf = pdfService.generateInvoice(hd);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("HD" + String.format("%04d", hd.getMaHD()) + ".pdf").build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(error("Lỗi in hóa đơn: " + e.getMessage()));
        }
    }

    // ==================== PRIVATE METHODS ====================
    private HoaDon findOrder(Integer id) {
        return hoaDonDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
    }

    private void checkStatus(HoaDon hd, String expected, String msg) {
        if (!expected.equals(hd.getTrangThai())) {
            throw new RuntimeException(msg);
        }
    }

    private void checkEmployee() {
        Users u = authService.getCurrentUser();
        if (u == null || u.getQuanTri() == null) {
            throw new RuntimeException("Không có quyền thực hiện thao tác này");
        }
    }

    private QuanTri getCurrentEmployee() {
        return authService.getCurrentUser().getQuanTri();
    }

    private List<HoaDon> filterByStatus(List<HoaDon> list, String status) {
        return list.stream().filter(h -> status.equals(h.getTrangThai())).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapList(List<HoaDon> list) {
        return list.stream().map(this::buildSummary).collect(Collectors.toList());
    }

    private Map<String, Object> buildSummary(HoaDon hd) {
        Map<String, Object> map = new HashMap<>();
        map.put("maHD", hd.getMaHD());
        map.put("maHDStr", String.format("HD%04d", hd.getMaHD()));
        map.put("ngayMua", hd.getNgayMua());
        map.put("ngayDen", hd.getNgayDen());
        map.put("trangThai", hd.getTrangThai());
        map.put("phuongThucTT", hd.getPhuongThucTT());
        map.put("ghiChu", hd.getGhiChu());

        if (hd.getKhachHang() != null) {
            map.put("maKH", hd.getKhachHang().getMaKH());
            map.put("tenKH", hd.getKhachHang().getTenKH());
            map.put("sdtKH", hd.getKhachHang().getSdt());
        }

        try {
            DiaChiJsonDTO dc = mapper.readValue(hd.getDiaChiJson(), DiaChiJsonDTO.class);
            map.put("sdt", dc.getSdt());
            map.put("tenNN", dc.getTenNN());
            map.put("diemGiao", dc.getDiemGiao());
        } catch (Exception e) {
            map.put("sdt", "");
            map.put("tenNN", "");
            map.put("diemGiao", "");
        }

        if (hd.getQuanTri() != null) {
            map.put("maQT", hd.getQuanTri().getMaQT());
            map.put("maQTStr", String.format("QT%04d", hd.getQuanTri().getMaQT()));
            map.put("tenQT", hd.getQuanTri().getTenQT());
            if (hd.getQuanTri().getUser() != null) {
                map.put("emailQT", hd.getQuanTri().getUser().getMail());
            }
        }

        double tong = hd.getHoaDonCTs().stream()
                .mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia()).sum();
        map.put("tongTien", tong);

        return map;
    }

    private Map<String, Object> buildDetail(HoaDon hd) {
        Map<String, Object> detail = buildSummary(hd);
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            Map<String, Object> item = new HashMap<>();
            item.put("maHDCT", ct.getMaHDCT());
            item.put("maSKU", ct.getSanPhamChiTiet().getMaSKU());
            item.put("tenSP", ct.getSanPhamChiTiet().getSanPham().getTenSP());
            item.put("tenMau", ct.getSanPhamChiTiet().getTenMau());
            item.put("coGiay", ct.getSanPhamChiTiet().getSize().getCoGiay());
            item.put("hinhAnh", ct.getSanPhamChiTiet().getHinhAnh());
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia());
            item.put("thanhTien", ct.getSoLuong() * ct.getDonGia());
            items.add(item);
        }
        detail.put("chiTiet", items);
        
        return detail;
    }

    private void sendSuccessEmail(HoaDon hd) {
    	try {
            KhachHang kh = hd.getKhachHang();
            if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) return;
            
            String email = kh.getUser().getMail();
            String tenKH = kh.getTenKH();
            
            byte[] pdfBytes = pdfService.generateInvoice(hd);
            
            String subject = "SHOEDO SHOP - Đơn hàng #HD" + String.format("%04d", hd.getMaHD()) + " đã giao thành công";
            String htmlContent = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'>"
                    + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{ padding: 20px; background: #f9f9f9;}.warning{background:#fff3cd;padding:10px;border-radius:5px;margin:15px 0}</style>"
                    + "</head><body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>ShoeDo Shop - Giao hàng thành công</h2></div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
                    + "<p>Đơn hàng <strong>#HD" + String.format("%04d", hd.getMaHD()) + "</strong> của bạn đã được giao thành công.</p>"
                    + "<p>Vui lòng xem file hóa đơn đính kèm để kiểm tra chi tiết đơn hàng.</p>"
                    + "<div class='warning'>"
                    + "<p><strong>Lưu ý:</strong> Bạn có <strong>3 NGÀY</strong> để kiểm tra đơn hàng và:</p>"
                    + "<p>• Báo lỗi nếu có vấn đề về sản phẩm</p>"
                    + "<p>• Yêu cầu hoàn hàng nếu không hài lòng</p>"
                    + "<p>Sau 3 ngày, đơn hàng sẽ được xác nhận hoàn tất và không thể thay đổi.</p>"
                    + "</div>"
                    + "<p>Cảm ơn bạn đã mua sắm tại ShoeDo Shop!</p>"
                    + "</div></div></body></html>";
            
            emailService.sendHtmlEmailWithAttachment(email, subject, htmlContent, 
                "HD" + String.format("%04d", hd.getMaHD()) + ".pdf", pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }

    private void sendApologyEmailWithPdf(HoaDon hd) {
    	try {
            KhachHang kh = hd.getKhachHang();
            if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) return;
            
            String email = kh.getUser().getMail();
            String tenKH = kh.getTenKH();
            
            byte[] pdfBytes = pdfService.generateInvoice(hd);
            
            String subject = "SHOEDO SHOP - Xin lỗi về sự cố đơn hàng #HD" + String.format("%04d", hd.getMaHD());
            String htmlContent = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'>"
                    + "<style>body{font-family:Arial,sans-serif}.container{max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:10px}.header{background:#000;color:#fff;padding:20px;text-align:center;border-radius:10px 10px 0 0}.content{ padding: 20px; background: #f9f9f9; }.apology{background:#f8d7da;color:#721c24;padding:15px;border-radius:5px;margin:15px 0}</style>"
                    + "</head><body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>ShoeDo Shop - Xin lỗi quý khách</h2></div>"
                    + "<div class='content'>"
                    + "<p>Xin chào <strong>" + tenKH + "</strong>,</p>"
                    + "<div class='apology'>"
                    + "<p>Chúng tôi chân thành xin lỗi về sự cố đơn hàng <strong>#HD" + String.format("%04d", hd.getMaHD()) + "</strong> mà bạn đã gặp phải.</p>"
                    + "<p><strong>Lỗi:</strong> " + (hd.getGhiChu() != null ? hd.getGhiChu() : "Không xác định") + "</p>"
                    + "</div>"
                    + "<p>Đội ngũ ShoeDo Shop đã xử lý sự cố này và đã khắc phục thành công. Vui lòng xem file hóa đơn đính kèm để kiểm tra chi tiết.</p>"
                    + "<p>Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ hotline 190001 của chúng tôi.</p>"
                    + "<p>Một lần nữa, chúng tôi xin lỗi về sự bất tiện này và hy vọng sẽ phục vụ bạn tốt hơn trong tương lai.</p>"
                    + "</div></div></body></html>";
            
            emailService.sendHtmlEmailWithAttachment(email, subject, htmlContent,
                "HD" + String.format("%04d", hd.getMaHD()) + ".pdf", pdfBytes);
            
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }

    private Map<String, Object> success(String key, Object value) {
        return Map.of("success", true, key, value);
    }

    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> success(String message) {
        return Map.of("success", true, "message", message);
    }

    private Map<String, Object> error(String message) {
        return Map.of("success", false, "message", message);
    }
}