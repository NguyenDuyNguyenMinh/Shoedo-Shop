package poly.edu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.*;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DonHangService {

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;

    @Autowired
    private DanhGiaDAO danhGiaDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== GET ORDERS ====================

    public Map<String, Object> getMyOrders(Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        List<HoaDon> allHoaDons = hoaDonDAO.findHoaDonsByCustomerId(khachHang.getMaKH());

        // Phân loại theo trạng thái
        Map<String, List<Map<String, Object>>> allOrders = new LinkedHashMap<>();
        allOrders.put("dangxuly", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Đang xử lý")));
        allOrders.put("danggiao", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Đang giao")));
        allOrders.put("datuchoi", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Đã từ chối")));
        allOrders.put("hoantat", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Hoàn tất")));
        allOrders.put("baoloi", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Báo lỗi")));
        allOrders.put("hoanhang", mapHoaDonToResponse(filterByTrangThai(allHoaDons, "Hoàn hàng/trả hàng")));

        // Đếm số lượng theo từng trạng thái
        Map<String, Integer> orderCounts = new HashMap<>();
        orderCounts.put("dangxuly", filterByTrangThai(allHoaDons, "Đang xử lý").size());
        orderCounts.put("danggiao", filterByTrangThai(allHoaDons, "Đang giao").size());
        orderCounts.put("datuchoi", filterByTrangThai(allHoaDons, "Đã từ chối").size());
        orderCounts.put("hoantat", filterByTrangThai(allHoaDons, "Hoàn tất").size());
        orderCounts.put("baoloi", filterByTrangThai(allHoaDons, "Báo lỗi").size());
        orderCounts.put("hoanhang", filterByTrangThai(allHoaDons, "Hoàn hàng/trả hàng").size());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy danh sách đơn hàng thành công");
        response.put("data", allOrders);
        response.put("counts", orderCounts);
        response.put("totalOrders", allHoaDons.size());

        return response;
    }

    public Map<String, Object> getOrderDetail(Integer orderId, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        HoaDon hoaDon = hoaDonDAO.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        Map<String, Object> orderDetail = buildOrderDetail(hoaDon);

        return Map.of(
                "success", true,
                "message", "Lấy chi tiết đơn hàng thành công",
                "order", orderDetail
        );
    }

    public Map<String, Object> getOrdersByStatus(String status, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        String actualStatus = mapStatusParam(status);
        List<HoaDon> allHoaDons = hoaDonDAO.findHoaDonsByCustomerId(khachHang.getMaKH());
        List<HoaDon> filteredOrders = filterByTrangThai(allHoaDons, actualStatus);

        return Map.of(
                "success", true,
                "message", "Lấy đơn hàng theo trạng thái thành công",
                "status", status,
                "data", mapHoaDonToResponse(filteredOrders)
        );
    }

    // ==================== ORDER ACTIONS ====================

    public Map<String, Object> updateOrderStatus(Integer orderId, String status, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        HoaDon hoaDon = hoaDonDAO.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này");
        }

        // Chỉ cho phép cập nhật từ "Đang giao" sang "Hoàn tất"
        if (!"Đang giao".equals(hoaDon.getTrangThai())) {
            return Map.of("success", false, "message", "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng đang ở trạng thái 'Đang giao'");
        }

        if (!"Hoàn tất".equals(status)) {
            return Map.of("success", false, "message", "Chỉ có thể cập nhật trạng thái sang 'Hoàn tất'");
        }

        hoaDon.setTrangThai("Hoàn tất");
        hoaDon.setNgayDen(new Date());
        hoaDonDAO.save(hoaDon);

        return Map.of(
                "success", true,
                "message", "Cập nhật trạng thái thành công",
                "order", buildOrderSummary(hoaDon)
        );
    }

    // Báo lỗi đơn hàng
    @Transactional
    public Map<String, Object> reportIssue(Map<String, Object> request, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        Integer orderId = (Integer) request.get("orderId");
        String reason = (String) request.get("reason");
        String note = (String) request.get("note");

        if (orderId == null) {
            return Map.of("success", false, "message", "Thiếu mã đơn hàng");
        }

        HoaDon hoaDon = hoaDonDAO.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new RuntimeException("Bạn không có quyền báo lỗi cho đơn này");
        }

        // Chỉ cho phép báo lỗi cho đơn đã nhận (Hoàn tất) hoặc đang giao
        if (!"Hoàn tất".equals(hoaDon.getTrangThai()) && !"Đang giao".equals(hoaDon.getTrangThai())) {
            return Map.of("success", false, "message", "Chỉ có thể báo lỗi cho đơn hàng đang giao hoặc đã nhận");
        }

        // Chuyển trạng thái sang Báo lỗi
        hoaDon.setTrangThai("Báo lỗi");

        // Thêm ghi chú
        String currentNote = hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "";
        String reportInfo = String.format("[BÁO LỖI - %s] Lý do: %s. %s",
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                reason,
                note != null ? note : "");

        if (currentNote.isEmpty()) {
            hoaDon.setGhiChu(reportInfo);
        } else {
            hoaDon.setGhiChu(currentNote + "\n" + reportInfo);
        }

        hoaDonDAO.save(hoaDon);

        return Map.of(
                "success", true,
                "message", "Báo lỗi đã được ghi nhận. Chúng tôi sẽ xử lý trong thời gian sớm nhất."
        );
    }

    // Đánh giá sản phẩm
    @Transactional
    public Map<String, Object> addReview(Map<String, Object> request, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        Integer maHDCT = (Integer) request.get("maHDCT");
        Integer sao = (Integer) request.get("sao");
        String danhGiaCT = (String) request.get("danhGiaCT");

        if (maHDCT == null) {
            return Map.of("success", false, "message", "Thiếu mã chi tiết đơn hàng");
        }

        if (sao == null || sao < 1 || sao > 5) {
            return Map.of("success", false, "message", "Đánh giá phải từ 1-5 sao");
        }

        HoaDonCT hoaDonCT = hoaDonCTDAO.findById(maHDCT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng"));

        HoaDon hoaDon = hoaDonCT.getHoaDon();

        // Kiểm tra quyền sở hữu
        if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new RuntimeException("Bạn không có quyền đánh giá sản phẩm này");
        }

        // Chỉ cho phép đánh giá đơn đã hoàn tất
        if (!"Hoàn tất".equals(hoaDon.getTrangThai())) {
            return Map.of("success", false, "message", "Chỉ có thể đánh giá đơn hàng đã hoàn tất");
        }

        // Kiểm tra đã đánh giá chưa
        if (hoaDonCT.getDanhGia() != null) {
            return Map.of("success", false, "message", "Sản phẩm này đã được đánh giá");
        }

        // Tạo đánh giá mới
        DanhGia danhGia = new DanhGia();
        danhGia.setHoaDonCT(hoaDonCT);
        danhGia.setSao(sao);
        danhGia.setDanhGiaCT(danhGiaCT);
        danhGia.setNgayDG(new Date());

        danhGiaDAO.save(danhGia);

        return Map.of(
                "success", true,
                "message", "Cảm ơn bạn đã đánh giá sản phẩm!",
                "danhGia", Map.of(
                        "maDG", danhGia.getMaDG(),
                        "sao", danhGia.getSao(),
                        "danhGiaCT", danhGia.getDanhGiaCT(),
                        "ngayDG", danhGia.getNgayDG()
                )
        );
    }

    // Lấy đánh giá của chi tiết đơn hàng
    public Map<String, Object> getReview(Integer maHDCT, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        HoaDonCT hoaDonCT = hoaDonCTDAO.findById(maHDCT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng"));

        DanhGia danhGia = hoaDonCT.getDanhGia();

        if (danhGia == null) {
            return Map.of("success", true, "daDanhGia", false);
        }

        return Map.of(
                "success", true,
                "daDanhGia", true,
                "danhGia", Map.of(
                        "maDG", danhGia.getMaDG(),
                        "sao", danhGia.getSao(),
                        "danhGiaCT", danhGia.getDanhGiaCT(),
                        "ngayDG", danhGia.getNgayDG()
                )
        );
    }

    // ==================== PRIVATE METHODS ====================

    private String mapStatusParam(String status) {
        switch (status.toLowerCase()) {
            case "dangxuly": return "Đang xử lý";
            case "danggiao": return "Đang giao";
            case "datuchoi": return "Đã từ chối";
            case "hoantat": return "Hoàn tất";
            case "baoloi": return "Báo lỗi";
            case "hoanhang": return "Hoàn hàng/trả hàng";
            default: return status;
        }
    }

    private List<HoaDon> filterByTrangThai(List<HoaDon> list, String trangThai) {
        if (trangThai == null || trangThai.isEmpty()) {
            return list;
        }
        return list.stream()
                .filter(h -> trangThai.equals(h.getTrangThai()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapHoaDonToResponse(List<HoaDon> hoaDons) {
        return hoaDons.stream().map(this::buildOrderSummary).collect(Collectors.toList());
    }

    private Map<String, Object> buildOrderSummary(HoaDon hd) {
        Map<String, Object> orderMap = new LinkedHashMap<>();
        orderMap.put("maHD", hd.getMaHD());
        orderMap.put("maHDStr", String.format("HD%04d", hd.getMaHD()));
        orderMap.put("ngayMua", hd.getNgayMua());
        orderMap.put("ngayDen", hd.getNgayDen());
        orderMap.put("trangThai", hd.getTrangThai());
        orderMap.put("phuongThucTT", hd.getPhuongThucTT());
        orderMap.put("ghiChu", hd.getGhiChu());
        orderMap.put("diaChiJson", hd.getDiaChiJson());

        parseDiaChi(hd, orderMap);

        if (hd.getDiaChiJson() != null && !hd.getDiaChiJson().isEmpty()) {
            try {
                DiaChiJsonDTO diaChi = objectMapper.readValue(hd.getDiaChiJson(), DiaChiJsonDTO.class);
                orderMap.put("tenNguoiNhan", diaChi.getTenNN());
                orderMap.put("sdtNguoiNhan", diaChi.getSdt());
                orderMap.put("diaChiGiaoHang", diaChi.getDiemGiao());
            } catch (Exception e) {
                if (hd.getKhachHang() != null) {
                    orderMap.put("tenNguoiNhan", hd.getKhachHang().getTenKH());
                    orderMap.put("sdtNguoiNhan", hd.getKhachHang().getSdt());
                }
            }
        } else if (hd.getKhachHang() != null) {
            orderMap.put("tenNguoiNhan", hd.getKhachHang().getTenKH());
            orderMap.put("sdtNguoiNhan", hd.getKhachHang().getSdt());
        }

        if (hd.getQuanTri() != null) {
            orderMap.put("maQT", hd.getQuanTri().getMaQT());
            orderMap.put("tenQT", hd.getQuanTri().getTenQT());
        }

        int totalItems = 0;
        double tongTien = 0.0;

        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            totalItems = hd.getHoaDonCTs().stream()
                    .mapToInt(HoaDonCT::getSoLuong)
                    .sum();

            tongTien = hd.getHoaDonCTs().stream()
                    .mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia())
                    .sum();

            HoaDonCT firstItem = hd.getHoaDonCTs().get(0);
            if (firstItem.getSanPhamChiTiet() != null) {
                orderMap.put("productImage", firstItem.getSanPhamChiTiet().getHinhAnh());
                orderMap.put("productName", firstItem.getSanPhamChiTiet().getSanPham() != null ?
                        firstItem.getSanPhamChiTiet().getSanPham().getTenSP() : "");

                // Thêm thông tin đánh giá
                orderMap.put("daDanhGia", firstItem.getDanhGia() != null);
            }
        }

        orderMap.put("tongTien", tongTien);
        orderMap.put("totalItems", totalItems);

        return orderMap;
    }

    private void parseDiaChi(HoaDon hd, Map<String, Object> orderMap) {
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
    }

    private Map<String, Object> buildOrderDetail(HoaDon hd) {
        Map<String, Object> detail = buildOrderSummary(hd);

        if (hd.getKhachHang() != null) {
            detail.put("maKH", hd.getKhachHang().getMaKH());
            detail.put("tenKH", hd.getKhachHang().getTenKH());
            detail.put("sdtKH", hd.getKhachHang().getSdt());
        }

        List<Map<String, Object>> chiTietList = new ArrayList<>();
        double tongTien = 0.0;

        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                Map<String, Object> ctMap = new LinkedHashMap<>();

                SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                if (spct != null) {
                    ctMap.put("maSKU", spct.getMaSKU());
                    ctMap.put("maHDCT", ct.getMaHDCT());

                    if (spct.getSanPham() != null) {
                        ctMap.put("tenSP", spct.getSanPham().getTenSP());
                        ctMap.put("moTa", spct.getSanPham().getMoTa());
                    }

                    ctMap.put("tenMau", spct.getTenMau());
                    ctMap.put("hinhAnh", spct.getHinhAnh());

                    if (spct.getSize() != null) {
                        ctMap.put("size", spct.getSize().getCoGiay());
                    }
                }

                ctMap.put("soLuong", ct.getSoLuong());
                ctMap.put("donGia", ct.getDonGia());
                double thanhTien = ct.getSoLuong() * ct.getDonGia();
                ctMap.put("thanhTien", thanhTien);

                // Thông tin đánh giá
                if (ct.getDanhGia() != null) {
                    ctMap.put("daDanhGia", true);
                    ctMap.put("danhGia", Map.of(
                            "maDG", ct.getDanhGia().getMaDG(),
                            "sao", ct.getDanhGia().getSao(),
                            "danhGiaCT", ct.getDanhGia().getDanhGiaCT(),
                            "ngayDG", ct.getDanhGia().getNgayDG()
                    ));
                } else {
                    ctMap.put("daDanhGia", false);
                }

                tongTien += thanhTien;
                chiTietList.add(ctMap);
            }
        }

        detail.put("chiTiet", chiTietList);
        detail.put("tongTien", tongTien);
        detail.put("soLuongSanPham", chiTietList.size());

        return detail;
    }
}