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
    @Autowired private KhachHangVoucherDAO khachHangVoucherDAO;
    @Autowired private LichSuTichDiemDAO lichSuTichDiemDAO;
    @Autowired private KhachHangDAO khachHangDAO;
    
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> getAllOrders() {
        Map<String, List<Map<String, Object>>> result = Map.of(
            "pending", new ArrayList<>(),
            "delivering", new ArrayList<>(),
            "completed", new ArrayList<>(),
            "rejected", new ArrayList<>(),
            "error", new ArrayList<>()
        );
        
        for (HoaDon hd : hoaDonDAO.findAll()) {
            Map<String, Object> summary = buildSummary(hd);
            if (hd.getTrangThai() == null) continue;
            switch (hd.getTrangThai()) {
                case "Đang xử lý": result.get("pending").add(summary); break;
                case "Đang giao": result.get("delivering").add(summary); break;
                case "Hoàn tất": result.get("completed").add(summary); break;
                case "Đã từ chối": result.get("rejected").add(summary); break;
                case "Báo lỗi": result.get("error").add(summary); break;
            }
        }
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

    @Transactional
    public Map<String, Object> confirmOrder(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang xử lý", "Chỉ có thể xác nhận đơn hàng ở trạng thái 'Đang xử lý'");
        checkEmployee();

        String phuongThucTT = hd.getPhuongThucTT();
        
        if ("COD".equals(phuongThucTT)) {
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
        }
        hd.setQuanTri(getCurrentEmployee());
        hd.setTrangThai("Đang giao");
        hoaDonDAO.save(hd);

        emailAsyncService.sendShippingEmail(hd);
        
        String message = "Đã vận chuyển đơn hàng";
        if ("COD".equals(phuongThucTT)) {
            message += " và trừ số lượng trong kho";
        } else {
            message += " (VNPAY - đã trừ số lượng khi đặt hàng)";
        }
        return success(message);
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
        String phuongThucTT = hd.getPhuongThucTT();

        if ("Đang xử lý".equals(current)) {
            if ("VNPAY".equals(phuongThucTT)) {
                for (HoaDonCT ct : hd.getHoaDonCTs()) {
                    spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
                }
            }
        } else if ("Đang giao".equals(current)) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
            }
        }

        if (hd.getKhachHangVoucher() != null) {
            KhachHangVoucher khv = hd.getKhachHangVoucher();
            khv.setTrangThai("Chưa sử dụng");
            khachHangVoucherDAO.save(khv);
            hd.setKhachHangVoucher(null);
        }

        hd.setQuanTri(getCurrentEmployee());
        hd.setTrangThai("Đã từ chối");
        hd.setGhiChu(lyDo);
        hoaDonDAO.save(hd);

        StringBuilder msg = new StringBuilder("Đã từ chối đơn hàng");

        if ("Đang xử lý".equals(current) && "VNPAY".equals(phuongThucTT)) {
            msg.append(" và hoàn trả số lượng về kho (VNPAY)");
        } else if ("Đang giao".equals(current)) {
            msg.append(" và hoàn trả số lượng về kho");
        } else if ("Đang xử lý".equals(current) && "COD".equals(phuongThucTT)) {
            msg.append(" (COD - chưa trừ số lượng)");
        }
        
        if (hd.getKhachHangVoucher() != null) {
            msg.append(" và hoàn trả voucher cho khách hàng");
        }
        
        return success(msg.toString());
    }

    @Transactional
    public Map<String, Object> deliveryFailed(Integer id, Map<String, String> payload) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang giao", "Chỉ có thể đánh dấu thất bại cho đơn hàng đang giao");

        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            spctDAO.congSoLuong(ct.getSanPhamChiTiet().getMaSKU(), ct.getSoLuong());
        }

        if (hd.getKhachHangVoucher() != null) {
            KhachHangVoucher khv = hd.getKhachHangVoucher();
            khv.setTrangThai("Chưa sử dụng");
            khachHangVoucherDAO.save(khv);
            hd.setKhachHangVoucher(null);
        }

        hd.setTrangThai("Đã từ chối");
        hd.setGhiChu(payload.getOrDefault("lyDo", "Giao hàng thất bại"));
        hd.setQuanTri(getCurrentEmployee());
        hoaDonDAO.save(hd);

        String msg = "Đã cập nhật giao hàng thất bại và hoàn trả số lượng về kho";
        if (hd.getKhachHangVoucher() != null) {
            msg += " và hoàn trả voucher cho khách hàng";
        }
        return success(msg);
    }

    @Transactional
    public Map<String, Object> deliverySuccess(Integer id) {
        HoaDon hd = findOrder(id);
        checkStatus(hd, "Đang giao", "Chỉ có thể đánh dấu thành công cho đơn hàng đang giao");

        for (HoaDonCT ct : hd.getHoaDonCTs()) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            SanPham sp = spct.getSanPham();
            int soLuongMoi = sp.getDaBan() + ct.getSoLuong();
            sp.setDaBan(soLuongMoi);
            sanPhamDAO.save(sp);
            
            if (ct.getNguoiChiaSe() != null) {
                KhachHang referrer = ct.getNguoiChiaSe();
                referrer.setDiemTichLuy(referrer.getDiemTichLuy() + 10);
                khachHangDAO.save(referrer);
                
                LichSuTichDiem history = new LichSuTichDiem();
                history.setKhachHang(referrer);
                history.setSoDiem(10);
                history.setLoaiGiaoDich("Chia sẻ mua hàng");
                history.setNgayGiaoDich(new Date());
                lichSuTichDiemDAO.save(history);
            }
        }


        hd.setTrangThai("Hoàn tất");
        hd.setNgayDen(new Date());
        hd.setQuanTri(getCurrentEmployee());
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
        
        hd.setTrangThai("Hoàn tất");
        hd.setQuanTri(getCurrentEmployee());
        hd.setGhiChu(null);
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

        double tongTien = hd.getHoaDonCTs().stream()
                .mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia()).sum();
        map.put("tongTien", tongTien);
        
        double voucherGiam = 0;
        if (hd.getKhachHangVoucher() != null && hd.getKhachHangVoucher().getVoucher() != null) {
            voucherGiam = hd.getKhachHangVoucher().getVoucher().getGiaTriGiam() != null ? 
                         hd.getKhachHangVoucher().getVoucher().getGiaTriGiam() : 0;
        }
        map.put("voucherGiam", voucherGiam);
        map.put("tongTienSauGiam", tongTien - Math.min(voucherGiam, tongTien));

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
        
        // Thêm thông tin voucher vào chi tiết
        if (hd.getKhachHangVoucher() != null && hd.getKhachHangVoucher().getVoucher() != null) {
            Map<String, Object> voucherInfo = new HashMap<>();
            voucherInfo.put("tenVoucher", hd.getKhachHangVoucher().getVoucher().getTenVoucher());
            voucherInfo.put("giaTriGiam", hd.getKhachHangVoucher().getVoucher().getGiaTriGiam());
            voucherInfo.put("donToiThieu", hd.getKhachHangVoucher().getVoucher().getDonToiThieu());
            detail.put("voucherApDung", voucherInfo);
        }
        
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