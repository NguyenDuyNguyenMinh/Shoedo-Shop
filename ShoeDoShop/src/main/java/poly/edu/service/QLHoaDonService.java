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
    @Autowired private SanPhamDAO sanPhamDAO;
    @Autowired private SanPhamChiTietDAO spctDAO;
    @Autowired private QuanTriDAO quanTriDAO;
    @Autowired private AuthService authService;
    @Autowired private EmailService emailService;
    @Autowired private EmailAsyncService emailAsyncService; 
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
    
    public Map<String, Object> getAllEmployees() {
        try {
            List<QuanTri> employees = quanTriDAO.findAll();
            
            List<Map<String, Object>> result = employees.stream()
                .filter(emp -> emp.getUser() != null && emp.getUser().getIsActive())
                .map(emp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maQT", emp.getMaQT());
                    map.put("tenQT", emp.getTenQT());
                    map.put("email", emp.getUser() != null ? emp.getUser().getMail() : null);
                    map.put("role", emp.getRole());
                    return map;
                })
                .collect(Collectors.toList());
            
            return success("data", result);
        } catch (Exception e) {
            return error("Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
        }
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

        // Kiểm tra đã trừ kho chưa (checkout() trừ kho rồi thì bỏ qua)
        boolean daTruKho = hd.getGhiChu() != null && hd.getGhiChu().contains("[DA_TRU_KHO]");

        if (!daTruKho) {
            // Chưa trừ kho → kiểm tra tồn kho và trừ
            List<String> outOfStock = new ArrayList<>();
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                if (ct.getSanPhamChiTiet().getSoLuong() < ct.getSoLuong()) {
                    String tenSP = ct.getSanPhamChiTiet().getSanPham() != null
                            ? ct.getSanPhamChiTiet().getSanPham().getTenSP()
                            : "SKU " + ct.getSanPhamChiTiet().getMaSKU();
                    outOfStock.add(tenSP);
                }
            }
            if (!outOfStock.isEmpty()) {
                return error("Sản phẩm không đủ số lượng: " + String.join(", ", outOfStock));
            }

            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                int updated = spctDAO.truSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
                if (updated == 0) {
                    String tenSP = ct.getSanPhamChiTiet().getSanPham() != null
                            ? ct.getSanPhamChiTiet().getSanPham().getTenSP()
                            : "SKU " + ct.getSanPhamChiTiet().getMaSKU();
                    return error("Sản phẩm \"" + tenSP + "\" không đủ tồn kho!");
                }
            }
        }

        hd.setQuanTri(getCurrentEmployee());
        hd.setTrangThai("Đang giao");
        hoaDonDAO.save(hd);

        emailAsyncService.sendShippingEmail(hd);
        return success("Đã vận chuyển đơn hàng");
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

        // Hoàn trả kho nếu đơn đã trừ kho (checkout) hoặc đang giao
        boolean daTruKho = hd.getGhiChu() != null && hd.getGhiChu().contains("[DA_TRU_KHO]");
        if (daTruKho || "Đang giao".equals(current)) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
            }
        }

        hd.setQuanTri(getCurrentEmployee());
        hd.setTrangThai("Đã từ chối");
        hd.setGhiChu(lyDo);
        hoaDonDAO.save(hd);

        String msg = "Đã từ chối đơn hàng";
        if (daTruKho || "Đang giao".equals(current)) msg += " và hoàn trả số lượng về kho";
        return success(msg);
    }

    @Transactional
    public Map<String, Object> deliveryFailed(Integer id, Map<String, String> payload) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang giao", "Chỉ có thể đánh dấu thất bại cho đơn hàng đang giao");

        // Hoàn trả kho nếu đơn đã trừ kho tại checkout
        boolean daTruKho = hd.getGhiChu() != null && hd.getGhiChu().contains("[DA_TRU_KHO]");
        if (daTruKho) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
            }
        }

        hd.setTrangThai("Đã từ chối");
        hd.setGhiChu(payload.getOrDefault("lyDo", "Giao hàng thất bại"));
        hd.setQuanTri(getCurrentEmployee());
        hoaDonDAO.save(hd);

        return success("Đã cập nhật giao hàng thất bại và hoàn trả số lượng về kho");
    }

    @Transactional
    public Map<String, Object> deliverySuccess(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang giao", "Chỉ có thể đánh dấu thành công cho đơn hàng đang giao");

        hd.setTrangThai("Hoàn tất");
        hd.setNgayDen(new Date());
        hd.setQuanTri(getCurrentEmployee());
        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            SanPham sp = spct.getSanPham();

            int soLuongMoi = sp.getDaBan() + ct.getSoLuong();
            sp.setDaBan(soLuongMoi);
            
            sanPhamDAO.save(sp);
        }
        
        hoaDonDAO.save(hd);
        
        emailAsyncService.sendSuccessEmail(hd);

        return success("Đã cập nhật giao hàng thành công. KH có 1 tháng để báo lỗi/bảo hành");
    }

    @Transactional
    public Map<String, Object> sendApologyEmail(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Báo lỗi", "Chỉ có thể gửi email xin lỗi cho đơn hàng báo lỗi");

        KhachHang kh = hd.getKhachHang();
        if (kh == null || kh.getUser() == null || kh.getUser().getMail() == null) {
            return error("Không tìm thấy email khách hàng");
        }
        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            spct.getMaSKU();
            spct.getTenMau();
            spct.getHinhAnh();
            spct.getSoLuong();
            
            SanPham sp = spct.getSanPham();
            sp.getTenSP();
            sp.getDaBan();

            if (spct.getSize() != null) {
                spct.getSize().getCoGiay();
            }
        }
        hd.setTrangThai("Hoàn tất");
        hd.setQuanTri(getCurrentEmployee());
        hoaDonDAO.save(hd);

        emailAsyncService.sendApologyEmail(hd);
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
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            item.put("maHDCT", ct.getMaHDCT());
            item.put("maSKU", spct != null ? spct.getMaSKU() : null);
            item.put("tenSP", (spct != null && spct.getSanPham() != null) ? spct.getSanPham().getTenSP() : "");
            item.put("tenMau", spct != null ? spct.getTenMau() : "");
            item.put("coGiay", (spct != null && spct.getSize() != null) ? spct.getSize().getCoGiay() : null);
            item.put("hinhAnh", spct != null ? spct.getHinhAnh() : "");
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia());
            item.put("thanhTien", ct.getSoLuong() * ct.getDonGia());
            items.add(item);
        }
        detail.put("chiTiet", items);
        
        return detail;
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
