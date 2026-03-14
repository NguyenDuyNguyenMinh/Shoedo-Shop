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
            return error("Số lượng tồn kho không đủ");
        }

        // Kiểm tra đã có trong giỏ chưa
        Optional<GioHang> existing = gioHangDAO.findByKhachHang_MaKHAndSanPhamChiTiet_MaSKU(
                kh.getMaKH(), dto.getMaSKU());

        if (existing.isPresent()) {
            GioHang gh = existing.get();
            int newQty = gh.getSoLuong() + dto.getSoLuong();
            if (newQty > spct.getSoLuong()) {
                return error("Số lượng vượt quá tồn kho (tồn: " + spct.getSoLuong() + ")");
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
            return error("Số lượng vượt quá tồn kho (tồn: " + gh.getSanPhamChiTiet().getSoLuong() + ")");
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

    // ==================== CHECKOUT ====================

    @Transactional
    public Map<String, Object> checkout(Users user, CheckoutDTO dto) {
        KhachHang kh = khachHangDAO.findByUser_MaUser(user.getMaUser());
        if (kh == null) {
            return error("Không tìm thấy thông tin khách hàng");
        }

        // Lấy danh sách sản phẩm trong giỏ hàng theo ID được chọn
        List<GioHang> selectedItems;
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            selectedItems = gioHangDAO.findAllById(dto.getCartItemIds());
            // Kiểm tra tất cả items thuộc về khách hàng hiện tại
            selectedItems = selectedItems.stream()
                    .filter(item -> item.getKhachHang().getMaKH().equals(kh.getMaKH()))
                    .collect(Collectors.toList());
        } else {
            selectedItems = gioHangDAO.findByKhachHang_MaKH(kh.getMaKH());
        }

        if (selectedItems.isEmpty()) {
            return error("Giỏ hàng trống hoặc không có sản phẩm nào được chọn");
        }

        // Kiểm tra tồn kho
        for (GioHang item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();
            if (spct.getSoLuong() < item.getSoLuong()) {
                String tenSP = spct.getSanPham() != null ? spct.getSanPham().getTenSP() : "SKU " + spct.getMaSKU();
                return error("Sản phẩm \"" + tenSP + "\" không đủ tồn kho (còn " + spct.getSoLuong() + ")");
            }
        }

        // Lấy địa chỉ giao hàng
        String diaChiJson = "";
        if (dto.getMaDC() != null) {
            DiaChi dc = diaChiDAO.findById(dto.getMaDC()).orElse(null);
            if (dc != null) {
                try {
                    Map<String, String> dcMap = new LinkedHashMap<>();
                    dcMap.put("TenNN", dc.getTenNN());
                    dcMap.put("SDT", dc.getSdt());
                    dcMap.put("DiemGiao", dc.getDiemGiao());
                    diaChiJson = objectMapper.writeValueAsString(dcMap);
                } catch (Exception e) {
                    diaChiJson = "";
                }
            }
        }

        // Xác định phương thức thanh toán
        String phuongThucTT = dto.getPhuongThucTT() != null ? dto.getPhuongThucTT() : "COD";
        boolean isVNPay = Boolean.TRUE.equals(dto.getIsVNPay()) || "VNPAY".equalsIgnoreCase(phuongThucTT);
        
        // Tạo hóa đơn
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(kh);
        hoaDon.setPhuongThucTT(isVNPay ? "VNPAY" : phuongThucTT);
        hoaDon.setDiaChiJson(diaChiJson);
        
        // Nếu là VNPAY, đặt trạng thái chờ thanh toán (dùng Đang xử lý vì DB chỉ có giá trị này)
        if (isVNPay) {
            hoaDon.setTrangThai("Đang xử lý");
        } else {
            hoaDon.setTrangThai("Đang xử lý");
        }
        
        hoaDon.setGhiChu(dto.getGhiChu());
        hoaDon.setNgayMua(new Date());
        hoaDon = hoaDonDAO.save(hoaDon);

        // Tạo chi tiết hóa đơn + trừ kho (chỉ khi không phải VNPAY hoặc đã thanh toán)
        double tongTien = 0;
        for (GioHang item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();

            // Tính giá
            double giaGoc = spct.getDonGia() != null ? spct.getDonGia() : 0;
            int km = (spct.getSanPham() != null && spct.getSanPham().getKhuyenMai() != null)
                    ? spct.getSanPham().getKhuyenMai()
                    : 0;
            double donGia = giaGoc * (100 - km) / 100;

            HoaDonCT hdct = new HoaDonCT();
            hdct.setHoaDon(hoaDon);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(item.getSoLuong());
            hdct.setDonGia(donGia);
            hoaDonCTDAO.save(hdct);

            tongTien += donGia * item.getSoLuong();
        }

        // Xóa các item đã checkout khỏi giỏ hàng
        for (GioHang item : selectedItems) {
            gioHangDAO.delete(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        if (isVNPay) {
            result.put("message", "Đơn hàng đã tạo. Vui lòng thanh toán VNPAY!");
            result.put("requirePayment", true);
        } else {
            result.put("message", "Đặt hàng thành công!");
        }
        
        result.put("maHD", hoaDon.getMaHD());
        result.put("tongTien", tongTien);
        result.put("isVNPay", isVNPay);
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
}
