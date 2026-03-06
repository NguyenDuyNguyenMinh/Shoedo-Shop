package poly.edu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.*;
import poly.edu.dto.DiaChiJsonDTO;
import poly.edu.entity.*;
import poly.edu.service.AuthService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/orders")
public class DonHangController {
    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;

    @Autowired
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy tất cả đơn hàng của khách hàng hiện tại (mặc định)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyOrders() {
        try {
            // Kiểm tra đăng nhập
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để xem đơn hàng"
                ));
            }

            // Lấy thông tin khách hàng từ user hiện tại
            KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (khachHang == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin khách hàng"
                ));
            }

            // Lấy tất cả đơn hàng của khách hàng
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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy chi tiết đơn hàng theo mã đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable("orderId") Integer orderId) {
        try {
            // Kiểm tra đăng nhập
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để xem chi tiết đơn hàng"
                ));
            }

            // Lấy thông tin khách hàng
            KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (khachHang == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin khách hàng"
                ));
            }

            // Lấy thông tin đơn hàng
            HoaDon hoaDon = hoaDonDAO.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Kiểm tra quyền sở hữu: đơn hàng này có thuộc về khách hàng này không?
            if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền xem đơn hàng này"
                ));
            }

            // Lấy chi tiết đơn hàng (bao gồm danh sách sản phẩm)
            Map<String, Object> orderDetail = buildOrderDetail(hoaDon);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy chi tiết đơn hàng thành công",
                    "order", orderDetail
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy đơn hàng theo trạng thái (bộ lọc)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable("status") String status) {
        try {
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (khachHang == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin khách hàng"
                ));
            }

            // Map status từ URL param sang trạng thái thực tế
            String actualStatus = mapStatusParam(status);

            List<HoaDon> allHoaDons = hoaDonDAO.findHoaDonsByCustomerId(khachHang.getMaKH());
            List<HoaDon> filteredOrders = filterByTrangThai(allHoaDons, actualStatus);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy đơn hàng theo trạng thái thành công",
                    "status", status,
                    "data", mapHoaDonToResponse(filteredOrders)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng (khách hàng xác nhận đã nhận hàng)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable("orderId") Integer orderId,
            @RequestParam("status") String status) {
        try {
            // Kiểm tra đăng nhập
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // Lấy thông tin khách hàng
            KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (khachHang == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin khách hàng"
                ));
            }

            // Kiểm tra đơn hàng tồn tại
            HoaDon hoaDon = hoaDonDAO.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Kiểm tra quyền sở hữu
            if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền cập nhật đơn hàng này"
                ));
            }

            // Chỉ cho phép cập nhật từ "Đang giao" sang "Hoàn tất"
            if (!"Đang giao".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng đang ở trạng thái 'Đang giao'"
                ));
            }

            if (!"Hoàn tất".equals(status)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể cập nhật trạng thái sang 'Hoàn tất'"
                ));
            }

            // Cập nhật trạng thái
            hoaDon.setTrangThai("Hoàn tất");
            hoaDon.setNgayDen(new Date()); // Cập nhật ngày nhận hàng
            hoaDonDAO.save(hoaDon);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "order", buildOrderSummary(hoaDon)
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi cập nhật trạng thái: " + e.getMessage()
            ));
        }
    }

    /**
     * Yêu cầu trả hàng
     */
    @PostMapping("/return")
    public ResponseEntity<Map<String, Object>> requestReturn(@RequestBody Map<String, Object> request) {
        try {
            // Kiểm tra đăng nhập
            Users currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // Lấy thông tin khách hàng
            KhachHang khachHang = khachHangDAO.findByUser_MaUser(currentUser.getMaUser());
            if (khachHang == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin khách hàng"
                ));
            }

            // Lấy dữ liệu từ request
            Integer orderId = (Integer) request.get("orderId");
            String reason = (String) request.get("reason");
            String note = (String) request.get("note");

            if (orderId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Thiếu mã đơn hàng"
                ));
            }

            // Kiểm tra đơn hàng tồn tại
            HoaDon hoaDon = hoaDonDAO.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Kiểm tra quyền sở hữu
            if (!hoaDon.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền yêu cầu trả hàng cho đơn này"
                ));
            }

            // Kiểm tra trạng thái đơn hàng (chỉ cho phép trả hàng khi đơn đã hoàn tất)
            if (!"Hoàn tất".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể yêu cầu trả hàng cho đơn đã hoàn tất"
                ));
            }

            // Kiểm tra thời gian (7 ngày kể từ ngày nhận)
            Date receivedDate = hoaDon.getNgayDen() != null ? hoaDon.getNgayDen() : hoaDon.getNgayMua();
            Calendar cal = Calendar.getInstance();
            cal.setTime(receivedDate);
            cal.add(Calendar.DAY_OF_MONTH, 7);
            Date deadline = cal.getTime();

            if (new Date().after(deadline)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đã quá thời hạn trả hàng (7 ngày kể từ ngày nhận)"
                ));
            }

            // Cập nhật trạng thái đơn hàng thành "Hoàn hàng/trả hàng"
            hoaDon.setTrangThai("Hoàn hàng/trả hàng");

            // Lưu lý do trả hàng vào ghi chú (có thể thêm trường riêng nếu cần)
            String currentNote = hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "";
            String returnInfo = String.format("[YÊU CẦU TRẢ HÀNG - %s] Lý do: %s. %s",
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                    reason,
                    note != null ? note : "");

            if (currentNote.isEmpty()) {
                hoaDon.setGhiChu(returnInfo);
            } else {
                hoaDon.setGhiChu(currentNote + "\n" + returnInfo);
            }

            hoaDonDAO.save(hoaDon);

            // TODO: Gửi email thông báo cho nhân viên xử lý (nếu cần)
            // emailService.sendReturnRequestNotification(hoaDon, reason, note);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Yêu cầu trả hàng đã được ghi nhận. Chúng tôi sẽ xử lý trong thời gian sớm nhất."
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi xử lý yêu cầu trả hàng: " + e.getMessage()
            ));
        }
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Map status parameter sang trạng thái thực tế trong database
     */
    private String mapStatusParam(String status) {
        switch (status.toLowerCase()) {
            case "dangxuly": return "Đang xử lý";
            case "danggiao": return "Đang giao";
            case "datuchoi": return "Đã từ chối";
            case "hoantat": return "Hoàn tất";
            case "baoloi": return "Báo lỗi";
            case "hoanhang": return "Hoàn hàng/trả hàng";
            default: return status; // Trả về nguyên gốc nếu không khớp
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

        // Parse địa chỉ từ JSON
        parseDiaChi(hd, orderMap);

        // Thông tin người nhận (lấy từ DiaChiJson)
        if (hd.getDiaChiJson() != null && !hd.getDiaChiJson().isEmpty()) {
            try {
                DiaChiJsonDTO diaChi = objectMapper.readValue(hd.getDiaChiJson(), DiaChiJsonDTO.class);
                orderMap.put("tenNguoiNhan", diaChi.getTenNN());
                orderMap.put("sdtNguoiNhan", diaChi.getSdt());
                orderMap.put("diaChiGiaoHang", diaChi.getDiemGiao());
            } catch (Exception e) {
                // Fallback to khách hàng info nếu không parse được
                if (hd.getKhachHang() != null) {
                    orderMap.put("tenNguoiNhan", hd.getKhachHang().getTenKH());
                    orderMap.put("sdtNguoiNhan", hd.getKhachHang().getSdt());
                }
            }
        } else if (hd.getKhachHang() != null) {
            orderMap.put("tenNguoiNhan", hd.getKhachHang().getTenKH());
            orderMap.put("sdtNguoiNhan", hd.getKhachHang().getSdt());
        }

        // Thông tin nhân viên xử lý (nếu có)
        if (hd.getQuanTri() != null) {
            orderMap.put("maQT", hd.getQuanTri().getMaQT());
            orderMap.put("tenQT", hd.getQuanTri().getTenQT());
        }

        // Tính tổng tiền và số lượng sản phẩm
        int totalItems = 0;
        double tongTien = 0.0;

        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            totalItems = hd.getHoaDonCTs().stream()
                    .mapToInt(HoaDonCT::getSoLuong)
                    .sum();

            tongTien = hd.getHoaDonCTs().stream()
                    .mapToDouble(ct -> ct.getSoLuong() * ct.getDonGia())
                    .sum();

            // Lấy ảnh đại diện của sản phẩm đầu tiên (nếu có)
            HoaDonCT firstItem = hd.getHoaDonCTs().get(0);
            if (firstItem.getSanPhamChiTiet() != null) {
                orderMap.put("productImage", firstItem.getSanPhamChiTiet().getHinhAnh());
                orderMap.put("productName", firstItem.getSanPhamChiTiet().getSanPham() != null ?
                        firstItem.getSanPhamChiTiet().getSanPham().getTenSP() : "");
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

        // Thông tin khách hàng chi tiết
        if (hd.getKhachHang() != null) {
            detail.put("maKH", hd.getKhachHang().getMaKH());
            detail.put("tenKH", hd.getKhachHang().getTenKH());
            detail.put("sdtKH", hd.getKhachHang().getSdt());
        }

        // Chi tiết sản phẩm trong đơn hàng
        List<Map<String, Object>> chiTietList = new ArrayList<>();
        double tongTien = 0.0;

        if (hd.getHoaDonCTs() != null && !hd.getHoaDonCTs().isEmpty()) {
            for (HoaDonCT ct : hd.getHoaDonCTs()) {
                Map<String, Object> ctMap = new LinkedHashMap<>();

                // Thông tin sản phẩm chi tiết
                SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                if (spct != null) {
                    ctMap.put("maSKU", spct.getMaSKU());

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

                // Thông tin mua hàng
                ctMap.put("soLuong", ct.getSoLuong());
                ctMap.put("donGia", ct.getDonGia());
                double thanhTien = ct.getSoLuong() * ct.getDonGia();
                ctMap.put("thanhTien", thanhTien);

                // Kiểm tra đánh giá (nếu có)
                if (ct.getDanhGia() != null) {
                    ctMap.put("daDanhGia", true);
                    ctMap.put("maDanhGia", ct.getDanhGia().getMaDG());
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