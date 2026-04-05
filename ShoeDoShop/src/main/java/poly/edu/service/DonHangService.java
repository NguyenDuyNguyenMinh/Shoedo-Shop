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

    // Hủy đơn hàng
    @Transactional
    public Map<String, Object> cancelOrder(Integer orderId, String cancelReason, Users currentUser) {
        try {
            // Lấy thông tin khách hàng
            KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (khachHang == null) {
                System.out.println("ERROR: Không tìm thấy khách hàng");
                return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
            }

            // Lấy đơn hàng
            HoaDon hoaDon = hoaDonDAO.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

            // Kiểm tra quyền sở hữu
            if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                System.out.println("ERROR: Không có quyền - order KH=" + hoaDon.getKhachHang().getMaKH() + ", current KH=" + khachHang.getMaKH());
                return Map.of("success", false, "message", "Bạn không có quyền hủy đơn hàng này");
            }

            // Kiểm tra trạng thái
            if (!"Đang xử lý".equals(hoaDon.getTrangThai())) {
                System.out.println("ERROR: Trạng thái không thể hủy: " + hoaDon.getTrangThai());
                return Map.of("success", false, "message", "Chỉ có thể hủy đơn hàng đang ở trạng thái 'Đang xử lý'");
            }

            // Kiểm tra phương thức thanh toán VNPAY không cho hủy
            if ("VNPAY".equals(hoaDon.getPhuongThucTT())) {
                return Map.of("success", false, "message", "Đơn hàng thanh toán qua VNPAY không thể hủy. Vui lòng liên hệ CSKH để được hỗ trợ.");
            }

            //Trả lại voucher nếu có áp dụng ***
            if (hoaDon.getKhachHangVoucher() != null) {
                KhachHangVoucher khachHangVoucher = hoaDon.getKhachHangVoucher();
                // Chỉ trả lại voucher nếu chưa sử dụng hoặc đang được áp dụng
                if ("Đã sử dụng".equals(khachHangVoucher.getTrangThai())) {
                    khachHangVoucher.setTrangThai("Chưa sử dụng");
                    // Có thể reset lại ngày nếu cần
                    // khachHangVoucher.setHanSuDung(originalExpiryDate);
                }
            }

            // Cập nhật trạng thái
            hoaDon.setTrangThai("Đã từ chối");

            // Thêm lý do hủy
            String currentNote = hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "";
            String timestamp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            String cancelInfo = String.format("[HỦY ĐƠN - %s] Lý do hủy: %s", timestamp, cancelReason);

            if (currentNote.isEmpty()) {
                hoaDon.setGhiChu(cancelInfo);
            } else {
                hoaDon.setGhiChu(currentNote + "\n" + cancelInfo);
            }

            hoaDonDAO.save(hoaDon);

            return Map.of(
                    "success", true,
                    "message", "Đơn hàng đã được hủy thành công",
                    "order", buildOrderSummary(hoaDon)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Chỉnh sửa đánh giá sản phẩm
    @Transactional
    public Map<String, Object> updateReview(Map<String, Object> request, Users currentUser) {
        KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
        if (khachHang == null) {
            return Map.of("success", false, "message", "Không tìm thấy thông tin khách hàng");
        }

        Integer maDG = (Integer) request.get("maDG");
        Integer sao = (Integer) request.get("sao");
        String danhGiaCT = (String) request.get("danhGiaCT");

        if (maDG == null) {
            return Map.of("success", false, "message", "Thiếu mã đánh giá");
        }

        if (sao == null || sao < 1 || sao > 5) {
            return Map.of("success", false, "message", "Đánh giá phải từ 1-5 sao");
        }

        DanhGia danhGia = danhGiaDAO.findById(maDG)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        HoaDonCT hoaDonCT = danhGia.getHoaDonCT();
        HoaDon hoaDon = hoaDonCT.getHoaDon();

        // Kiểm tra quyền sở hữu
        if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        // Cập nhật đánh giá
        danhGia.setSao(sao);
        danhGia.setDanhGiaCT(danhGiaCT);
        danhGia.setNgayDG(new Date());

        danhGiaDAO.save(danhGia);

        return Map.of(
                "success", true,
                "message", "Đánh giá đã được cập nhật thành công",
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
        double tongTienGoc = 0.0;           // Tổng tiền gốc (chưa KM sản phẩm)
        double tongTienSauKmSp = 0.0;       // Tổng tiền sau khuyến mãi sản phẩm (đã trừ % KM)
        double tongGiamGiaKmSp = 0.0;       // Tổng tiền giảm từ khuyến mãi sản phẩm
        double tongGiamGiaVoucher = 0.0;    // Tổng tiền giảm từ voucher

        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            totalItems = hd.getHoaDonCTs().stream()
                    .mapToInt(HoaDonCT::getSoLuong)
                    .sum();

            // Tính tổng tiền gốc và tổng tiền sau khuyến mãi sản phẩm
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                double giaGoc = getGiaGocSanPham(ct);  // Lấy giá gốc từ SanPhamChiTiet
                double giaSauKm = ct.getDonGia();      // Giá đã lưu là giá sau KM sản phẩm

                double thanhTienGoc = ct.getSoLuong() * giaGoc;
                double thanhTienSauKm = ct.getSoLuong() * giaSauKm;

                tongTienGoc += thanhTienGoc;
                tongTienSauKmSp += thanhTienSauKm;
            }

            tongGiamGiaKmSp = tongTienGoc - tongTienSauKmSp;

            // Tính giảm giá từ voucher (áp dụng cho toàn đơn sau khi đã tính KM sản phẩm)
            if (hd.getKhachHangVoucher() != null && hd.getKhachHangVoucher().getVoucher() != null) {
                Voucher voucher = hd.getKhachHangVoucher().getVoucher();
                Map<String, Object> voucherInfo = new LinkedHashMap<>();
                voucherInfo.put("maVoucher", voucher.getMaVoucher());
                voucherInfo.put("tenVoucher", voucher.getTenVoucher());
                voucherInfo.put("donToiThieu", voucher.getDonToiThieu());
                voucherInfo.put("diemCanDoi", voucher.getDiemCanDoi());

                // Chỉ có giảm theo số tiền (GiaTriGiam)
                if (voucher.getGiaTriGiam() != null && voucher.getGiaTriGiam() > 0) {
                    tongGiamGiaVoucher = voucher.getGiaTriGiam();
                    if (tongGiamGiaVoucher > tongTienSauKmSp) {
                        tongGiamGiaVoucher = tongTienSauKmSp;
                    }
                    voucherInfo.put("giaTriGiam", voucher.getGiaTriGiam());
                }

                orderMap.put("voucherApDung", voucherInfo);
            }

            HoaDonCT firstItem = hd.getHoaDonCTs().get(0);
            if (firstItem.getSanPhamChiTiet() != null) {
                orderMap.put("productImage", firstItem.getSanPhamChiTiet().getHinhAnh());
                orderMap.put("productName", firstItem.getSanPhamChiTiet().getSanPham() != null ?
                        firstItem.getSanPhamChiTiet().getSanPham().getTenSP() : "");
                orderMap.put("daDanhGia", firstItem.getDanhGia() != null);
            }
        }

        double tongTienCuoiCung = tongTienSauKmSp - tongGiamGiaVoucher;
        if (tongTienCuoiCung < 0) tongTienCuoiCung = 0;

        orderMap.put("tongTienGoc", tongTienGoc);                    // Tổng tiền gốc
        orderMap.put("tongGiamGiaKmSp", tongGiamGiaKmSp);           // Giảm từ KM sản phẩm
        orderMap.put("tongTienSauKmSp", tongTienSauKmSp);           // Tiền sau KM sản phẩm
        orderMap.put("tongGiamGiaVoucher", tongGiamGiaVoucher);     // Giảm từ voucher
        orderMap.put("tongTien", tongTienCuoiCung);                 // Tiền cuối cùng phải trả
        orderMap.put("totalItems", totalItems);

        return orderMap;
    }

    /**
     * Lấy giá gốc của sản phẩm (trước khuyến mãi)
     * donGia trong HoaDonCT là giá sau KM sản phẩm
     */
    private double getGiaGocSanPham(HoaDonCT ct) {
        SanPhamChiTiet spct = ct.getSanPhamChiTiet();
        if (spct == null) {
            return ct.getDonGia(); // fallback
        }

        double giaGoc = spct.getDonGia(); // Giá gốc từ bảng SanPhamChiTiet
        double khuyenMai = 0;

        if (spct.getSanPham() != null && spct.getSanPham().getKhuyenMai() != null) {
            khuyenMai = spct.getSanPham().getKhuyenMai();
        }

        // Nếu có khuyến mãi %, tính ngược lại giá gốc
        // ct.getDonGia() = giaGoc * (100 - khuyenMai) / 100
        // => giaGoc = ct.getDonGia() * 100 / (100 - khuyenMai)
        if (khuyenMai > 0 && ct.getDonGia() != null && ct.getDonGia() > 0) {
            giaGoc = ct.getDonGia() * 100 / (100 - khuyenMai);
            // Làm tròn đến 2 chữ số thập phân
            giaGoc = Math.round(giaGoc * 100.0) / 100.0;
        }

        return giaGoc;
    }

    private Map<String, Object> buildOrderDetail(HoaDon hd) {
        Map<String, Object> detail = buildOrderSummary(hd);

        if (hd.getKhachHang() != null) {
            detail.put("maKH", hd.getKhachHang().getMaKH());
            detail.put("tenKH", hd.getKhachHang().getTenKH());
            detail.put("sdtKH", hd.getKhachHang().getSdt());
        }

        List<Map<String, Object>> chiTietList = new ArrayList<>();
        double tongTienGoc = 0.0;
        double tongTienSauKmSp = 0.0;
        double tongGiamGiaKmSp = 0.0;

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
                        ctMap.put("khuyenMaiSP", spct.getSanPham().getKhuyenMai() != null ?
                                spct.getSanPham().getKhuyenMai() : 0);
                    }

                    ctMap.put("tenMau", spct.getTenMau());
                    ctMap.put("hinhAnh", spct.getHinhAnh());

                    if (spct.getSize() != null) {
                        ctMap.put("size", spct.getSize().getCoGiay());
                    }
                }

                ctMap.put("soLuong", ct.getSoLuong());

                // Tính giá gốc
                double giaGoc = getGiaGocSanPham(ct);
                double giaSauKmSp = ct.getDonGia();
                double khuyenMaiPhanTram = 0;

                if (spct != null && spct.getSanPham() != null && spct.getSanPham().getKhuyenMai() != null) {
                    khuyenMaiPhanTram = spct.getSanPham().getKhuyenMai();
                }

                double giamGiaKmSp = giaGoc - giaSauKmSp;
                if (giamGiaKmSp < 0) giamGiaKmSp = 0;

                ctMap.put("giaGoc", giaGoc);
                ctMap.put("khuyenMaiPhanTram", khuyenMaiPhanTram);
                ctMap.put("giamGiaKmSp", giamGiaKmSp);
                ctMap.put("donGia", giaSauKmSp);

                double thanhTienGoc = ct.getSoLuong() * giaGoc;
                double thanhTienSauKmSp = ct.getSoLuong() * giaSauKmSp;
                double giamGiaSp = thanhTienGoc - thanhTienSauKmSp;
                if (giamGiaSp < 0) giamGiaSp = 0;

                ctMap.put("thanhTienGoc", thanhTienGoc);
                ctMap.put("giamGiaSp", giamGiaSp);
                ctMap.put("thanhTienSauKmSp", thanhTienSauKmSp);
                ctMap.put("thanhTien", thanhTienSauKmSp);

                tongTienGoc += thanhTienGoc;
                tongTienSauKmSp += thanhTienSauKmSp;
                tongGiamGiaKmSp += giamGiaSp;

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

                chiTietList.add(ctMap);
            }
        }

        detail.put("chiTiet", chiTietList);
        detail.put("tongTienGoc", tongTienGoc);
        detail.put("tongGiamGiaKmSp", tongGiamGiaKmSp);
        detail.put("tongTienSauKmSp", tongTienSauKmSp);
        detail.put("soLuongSanPham", chiTietList.size());

        return detail;
    }
}