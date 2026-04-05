package poly.edu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.*;
import poly.edu.dto.CheckoutDTO;
import poly.edu.dto.GioHangDTO;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GioHangService {

    @Autowired
    private GioHangDAO gioHangDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;

    @Autowired
    private DiaChiDAO diaChiDAO;

    @Autowired
    private KhachHangVoucherDAO khachHangVoucherDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== GET CART ====================

    public Map<String, Object> getCart(Users user) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) {
            return error("Không tìm thấy thông tin khách hàng");
        }

        List<GioHang> cartItems = gioHangDAO.findByKhachHang_MaKH(kh.getMaKH());
        List<Map<String, Object>> items = cartItems.stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("maGH", item.getMaGH());
            map.put("soLuong", item.getSoLuong());

            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            if (spct != null) {
                map.put("maSKU", spct.getMaSKU());
                map.put("tenMau", spct.getTenMau());
                map.put("hinhAnh", spct.getHinhAnh());
                map.put("donGia", spct.getDonGia());
                map.put("soLuongTon", spct.getSoLuong());
                map.put("trangThai", spct.getTrangThai());

                if (spct.getSize() != null) {
                    map.put("size", spct.getSize().getCoGiay());
                }

                if (spct.getSanPham() != null) {
                    SanPham sp = spct.getSanPham();
                    map.put("maSP", sp.getMaSP());
                    map.put("tenSP", sp.getTenSP());
                    map.put("moTa", sp.getMoTa());
                    map.put("khuyenMai", sp.getKhuyenMai());

                    // Tính giá sau khuyến mãi
                    double giaGoc = spct.getDonGia() != null ? spct.getDonGia() : 0;
                    int km = sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0;
                    double giaSauKM = giaGoc * (100 - km) / 100;
                    map.put("giaGoc", giaGoc);
                    map.put("giaSauKM", giaSauKM);
                    map.put("thanhTien", giaSauKM * item.getSoLuong());
                }
            }

            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("items", items);
        result.put("totalItems", items.size());

        return result;
    }

    // ==================== ADD TO CART ====================

    public Map<String, Object> addToCart(Users user, GioHangDTO dto) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) {
            return error("Không tìm thấy thông tin khách hàng");
        }

        SanPhamChiTiet spct = sanPhamChiTietDAO.findById(dto.getMaSKU()).orElse(null);
        if (spct == null) {
            return error("Sản phẩm không tồn tại");
        }

        if (spct.getSoLuong() == null || spct.getSoLuong() < dto.getSoLuong()) {
            return error("Số lượng vượt quá giới hạn cho phép");
        }

        // Kiểm tra đã có trong giỏ chưa
        Optional<GioHang> existing = gioHangDAO.findByKhachHang_MaKHAndSanPhamChiTiet_MaSKU(
                kh.getMaKH(), dto.getMaSKU());

        if (existing.isPresent()) {
            GioHang gh = existing.get();
            int newQty = gh.getSoLuong() + dto.getSoLuong();
            if (newQty > spct.getSoLuong()) {
                return error("Số lượng vượt quá giới hạn cho phép");
            }
            gh.setSoLuong(newQty);
            gioHangDAO.save(gh);
        } else {
            GioHang gh = new GioHang();
            gh.setKhachHang(kh);
            gh.setSanPhamChiTiet(spct);
            gh.setSoLuong(dto.getSoLuong());
            gioHangDAO.save(gh);
        }

        Integer cartCount = gioHangDAO.countByKhachHangMaKH(kh.getMaKH());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã thêm vào giỏ hàng");
        result.put("cartCount", cartCount);
        return result;
    }

    // ==================== UPDATE CART ITEM ====================

    public Map<String, Object> updateCartItem(Users user, Integer maGH, Integer soLuong) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) {
            return error("Không tìm thấy thông tin khách hàng");
        }

        GioHang gh = gioHangDAO.findById(maGH).orElse(null);
        if (gh == null) {
            return error("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        if (!gh.getKhachHang().getMaKH().equals(kh.getMaKH())) {
            return error("Bạn không có quyền cập nhật mục này");
        }

        if (soLuong <= 0) {
            gioHangDAO.delete(gh);
            return success("Đã xóa sản phẩm khỏi giỏ hàng");
        }

        if (gh.getSanPhamChiTiet().getSoLuong() < soLuong) {
            return error("Số lượng vượt quá giới hạn cho phép");
        }

        gh.setSoLuong(soLuong);
        gioHangDAO.save(gh);

        return success("Cập nhật số lượng thành công");
    }

    // ==================== REMOVE FROM CART ====================

    public Map<String, Object> removeFromCart(Users user, Integer maGH) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) {
            return error("Không tìm thấy thông tin khách hàng");
        }

        GioHang gh = gioHangDAO.findById(maGH).orElse(null);
        if (gh == null) {
            return error("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        if (!gh.getKhachHang().getMaKH().equals(kh.getMaKH())) {
            return error("Bạn không có quyền xóa mục này");
        }

        gioHangDAO.delete(gh);

        Integer cartCount = gioHangDAO.countByKhachHangMaKH(kh.getMaKH());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
        result.put("cartCount", cartCount);
        return result;
    }

    // ==================== GET CART COUNT ====================

    public Map<String, Object> getCartCount(Users user) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        Integer count = 0;
        if (kh != null) {
            count = gioHangDAO.countByKhachHangMaKH(kh.getMaKH());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("cartCount", count != null ? count : 0);
        return result;
    }

    // ==================== VALIDATE CHECKOUT (cho VNPay) ====================

    /**
     * Chỉ validate dữ liệu checkout và tính tổng tiền, KHÔNG tạo HoaDon.
     * Dùng cho VNPay: validate trước khi redirect sang cổng thanh toán.
     */
    public Map<String, Object> validateCheckout(Users user, CheckoutDTO dto) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) return error("Không tìm thấy thông tin khách hàng");

        List<GioHang> selectedItems;
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            selectedItems = gioHangDAO.findAllById(dto.getCartItemIds());
            selectedItems = selectedItems.stream()
                    .filter(item -> item.getKhachHang().getMaKH().equals(kh.getMaKH()))
                    .collect(Collectors.toList());
        } else {
            selectedItems = gioHangDAO.findByKhachHang_MaKH(kh.getMaKH());
        }
        if (selectedItems.isEmpty()) return error("Giỏ hàng trống hoặc không có sản phẩm nào được chọn");

        for (GioHang item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            if (spct.getSoLuong() < item.getSoLuong()) {
                String tenSP = spct.getSanPham() != null ? spct.getSanPham().getTenSP() : "SKU " + spct.getMaSKU();
                return error("Sản phẩm \"" + tenSP + "\" vượt quá giới hạn cho phép");
            }
        }

        double tongTien = 0;
        for (GioHang item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            double giaGoc = spct.getDonGia() != null ? spct.getDonGia() : 0;
            int km = (spct.getSanPham() != null && spct.getSanPham().getKhuyenMai() != null)
                     ? spct.getSanPham().getKhuyenMai() : 0;
            tongTien += giaGoc * (100 - km) / 100 * item.getSoLuong();
        }

        double voucherDiscount = 0;
        if (dto.getMaKH_VC() != null) {
            var khvOpt = khachHangVoucherDAO.findByMaKHVCAndMaKH(dto.getMaKH_VC(), kh.getMaKH());
            if (khvOpt.isEmpty()) return error("Voucher không hợp lệ hoặc không thuộc về bạn");
            KhachHangVoucher v = khvOpt.get();
            if (!"Chưa sử dụng".equals(v.getTrangThai())) return error("Voucher này đã được sử dụng");
            if (v.getHanSuDung() != null && v.getHanSuDung().before(new Date())) return error("Voucher đã hết hạn");
            Voucher voucher = v.getVoucher();
            if (voucher.getIsActive() == null || !voucher.getIsActive()) return error("Voucher đã bị vô hiệu hóa");
            double donToiThieu = voucher.getDonToiThieu() != null ? voucher.getDonToiThieu() : 0;
            if (tongTien < donToiThieu) return error("Đơn hàng tối thiểu " + formatCurrency(donToiThieu) + " mới áp dụng được voucher này");
            voucherDiscount = voucher.getGiaTriGiam() != null ? voucher.getGiaTriGiam() : 0;
        }

        double tongTienSauGiam = Math.max(0, tongTien - voucherDiscount);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tongTien", tongTien);
        result.put("voucherDiscount", voucherDiscount);
        result.put("tongTienSauGiam", tongTienSauGiam);
        return result;
    }

    // ==================== CHECKOUT ====================

    @Transactional
    public Map<String, Object> checkout(Users user, CheckoutDTO dto) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) {
            return error("Không tìm thấy thông tin khách hàng");
        }
        List<GioHang> selectedItems;
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            selectedItems = gioHangDAO.findAllById(dto.getCartItemIds());
            selectedItems = selectedItems.stream()
                    .filter(item -> item.getKhachHang().getMaKH().equals(kh.getMaKH()))
                    .collect(Collectors.toList());
        } else {
            selectedItems = gioHangDAO.findByKhachHang_MaKH(kh.getMaKH());
        }

        if (selectedItems.isEmpty()) {
            return error("Giỏ hàng trống hoặc không có sản phẩm nào được chọn");
        }

        Map<String, String> refMap = new HashMap<>();
        if (dto.getRefCode() != null && dto.getRefCode().trim().startsWith("{")) {
            try {
                refMap = objectMapper.readValue(dto.getRefCode().trim(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
            } catch (Exception e) {
                System.out.println("Lỗi parse JSON refMap: " + e.getMessage());
            }
        }

        for (GioHang item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            if (spct.getSoLuong() < item.getSoLuong()) {
                String tenSP = spct.getSanPham() != null
                        ? spct.getSanPham().getTenSP() : "SKU " + spct.getMaSKU();
                return error("Sản phẩm \"" + tenSP + "\" vượt quá giới hạn cho phép");
            }
        }

        // ── Lấy địa chỉ giao hàng ──
        String diaChiJson = "";
        if (dto.getMaDC() != null) {
            DiaChi dc = diaChiDAO.findById(dto.getMaDC()).orElse(null);
            if (dc != null) {
                try {
                    Map<String, String> dcMap = new LinkedHashMap<>();
                    dcMap.put("TenNN",   dc.getTenNN());
                    dcMap.put("SDT",    dc.getSdt());
                    dcMap.put("DiemGiao", dc.getDiemGiao());
                    diaChiJson = objectMapper.writeValueAsString(dcMap);
                } catch (Exception e) {
                    diaChiJson = "";
                }
            }
        }

        // ── Xác định phương thức thanh toán ──
        String phuongThucTT = dto.getPhuongThucTT() != null ? dto.getPhuongThucTT() : "COD";
        boolean isVNPay = Boolean.TRUE.equals(dto.getIsVNPay())
                       || "VNPAY".equalsIgnoreCase(phuongThucTT);

        // ── Xử lý voucher ──
        KhachHangVoucher usedVoucher = null;
        double voucherDiscount = 0.0;

        if (dto.getMaKH_VC() != null) {
            var khvOpt = khachHangVoucherDAO.findByMaKHVCAndMaKH(dto.getMaKH_VC(), kh.getMaKH());
            if (khvOpt.isEmpty()) {
                return error("Voucher không hợp lệ hoặc không thuộc về bạn");
            }
            usedVoucher = khvOpt.get();

            // Kiểm tra voucher còn hạn và chưa dùng
            if (!"Chưa sử dụng".equals(usedVoucher.getTrangThai())) {
                return error("Voucher này đã được sử dụng");
            }
            Date now = new Date();
            if (usedVoucher.getHanSuDung() != null && usedVoucher.getHanSuDung().before(now)) {
                return error("Voucher đã hết hạn");
            }
            Voucher voucher = usedVoucher.getVoucher();
            if (voucher.getIsActive() == null || !voucher.getIsActive()) {
                return error("Voucher đã bị vô hiệu hóa");
            }

            // Tính tongTien trước để kiểm tra đơn tối thiểu
            double tempTong = 0;
            for (GioHang item : selectedItems) {
                SanPhamChiTiet spct = item.getSanPhamChiTiet();
                double giaGoc = spct.getDonGia() != null ? spct.getDonGia() : 0;
                int km = (spct.getSanPham() != null && spct.getSanPham().getKhuyenMai() != null)
                         ? spct.getSanPham().getKhuyenMai() : 0;
                double donGia = giaGoc * (100 - km) / 100;
                tempTong += donGia * item.getSoLuong();
            }

            double donToiThieu = voucher.getDonToiThieu() != null ? voucher.getDonToiThieu() : 0;
            if (tempTong < donToiThieu) {
                return error("Đơn hàng tối thiểu " + formatCurrency(donToiThieu) + " mới áp dụng được voucher này");
            }

            // Gia trị giảm giá
            voucherDiscount = voucher.getGiaTriGiam() != null ? voucher.getGiaTriGiam() : 0;
        }

        // ── Tạo hóa đơn ──
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(kh);
        hoaDon.setPhuongThucTT(isVNPay ? "VNPAY" : phuongThucTT);
        hoaDon.setDiaChiJson(diaChiJson);
        hoaDon.setTrangThai("Đang xử lý");
        hoaDon.setGhiChu(dto.getGhiChu());
        hoaDon.setNgayMua(new Date());

        // Gắn voucher nếu có
        if (usedVoucher != null) {
            hoaDon.setKhachHangVoucher(usedVoucher);
        }

        hoaDon = hoaDonDAO.save(hoaDon);

        // ── Tạo chi tiết hóa đơn & tính tongTien ──
        double tongTien = 0;
        for (GioHang item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();

            double giaGoc = spct.getDonGia() != null ? spct.getDonGia() : 0;
            int km = (spct.getSanPham() != null && spct.getSanPham().getKhuyenMai() != null)
                     ? spct.getSanPham().getKhuyenMai() : 0;
            double donGia = giaGoc * (100 - km) / 100;

            HoaDonCT hdct = new HoaDonCT();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(donGia);
            String skuKey = String.valueOf(spct.getMaSKU());
            if (refMap.containsKey(skuKey)) {
                try {
                    // Lọc lấy số (chống rác)
                    String numericCode = refMap.get(skuKey).replaceAll("[^0-9]", "");
                    if (!numericCode.isEmpty()) {
                        Integer maNguoiChiaSe = Integer.parseInt(numericCode);
                        KhachHang nguoiChiaSe = khachHangDAO.findByMaKH(maNguoiChiaSe);
                        
                        // Chặn tự mua để tự lấy điểm
                        if (nguoiChiaSe != null && !nguoiChiaSe.getMaKH().equals(kh.getMaKH())) {
                            hdct.setNguoiChiaSe(nguoiChiaSe);
                        }
                    }
                } catch (Exception e) {}
            }
            hoaDonCTDAO.save(hdct);

            tongTien += donGia * item.getSoLuong();

            if (isVNPay) {
                sanPhamChiTietDAO.truSoLuong(spct.getMaSKU(), item.getSoLuong());
            }
        }

        double tongTienSauGiam = Math.max(0, tongTien - voucherDiscount);
        
        
        for (GioHang item : selectedItems) {
            gioHangDAO.delete(item);
        }

        // ── Đánh dấu voucher là đã sử dụng ──
        if (usedVoucher != null) {
            usedVoucher.setTrangThai("Đã sử dụng");
            usedVoucher.setNgayDoi(new Date());
            khachHangVoucherDAO.save(usedVoucher);
        }

        // ── Kết quả ──
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("maHD", hoaDon.getMaHD());
        result.put("tongTien", tongTien);
        result.put("voucherDiscount", voucherDiscount);
        result.put("tongTienSauGiam", tongTienSauGiam);
        result.put("isVNPay", isVNPay);

        result.put("message", "Đặt hàng thành công!");

        return result;
    }

    // ==================== UTILS ====================

    private Map<String, Object> success(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    private String formatCurrency(double value) {
        return String.format("%,.0f₫", (long) value);
    }
}
