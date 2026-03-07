package poly.edu.service.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.DiaChiDAO;
import poly.edu.dao.GioHangDAO;
import poly.edu.dao.HoaDonCTDAO;
import poly.edu.dao.HoaDonDAO;
import poly.edu.dao.KhachHangDAO;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dto.checkout.CheckoutRequestDTO;
import poly.edu.dto.checkout.CheckoutResponseDTO;
import poly.edu.entity.DiaChi;
import poly.edu.entity.GioHang;
import poly.edu.entity.HoaDon;
import poly.edu.entity.HoaDonCT;
import poly.edu.entity.KhachHang;
import poly.edu.entity.SanPhamChiTiet;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;

    @Autowired
    private GioHangDAO gioHangDAO;

    @Autowired
    private KhachHangDAO khachHangDAO;

    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;

    @Autowired
    private DiaChiDAO diaChiDAO;

    /**
     * Tạo JSON string từ thông tin địa chỉ
     */
    private String createDiaChiJson(DiaChi diaChi) {
        return String.format(
            "{\"tenNN\":\"%s\",\"sdt\":\"%s\",\"diemGiao\":\"%s\",\"macDinh\":%s}",
            escapeJson(diaChi.getTenNN()),
            escapeJson(diaChi.getSdt()),
            escapeJson(diaChi.getDiemGiao()),
            diaChi.getMacDinh() != null && diaChi.getMacDinh() ? "true" : "false"
        );
    }

    /**
     * Escape ký tự đặc biệt trong JSON
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * Thực hiện đặt hàng (Checkout) với COD
     * Toàn bộ transaction sẽ rollback nếu có bất kỳ lỗi nào
     *
     * @param request Thông tin đặt hàng
     * @return CheckoutResponseDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponseDTO processCheckout(CheckoutRequestDTO request) {
        // Bước 1: Validate và lấy thông tin khách hàng
        KhachHang khachHang = khachHangDAO.findById(request.getMaKH())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với mã: " + request.getMaKH()));

        // Bước 2: Lấy danh sách sản phẩm từ giỏ hàng
        List<GioHang> gioHangList;
        if (request.getDanhSachMaSKU() != null && !request.getDanhSachMaSKU().isEmpty()) {
            // Chỉ lấy các sản phẩm được chọn
            gioHangList = gioHangDAO.findByKhachHangMaKH(request.getMaKH())
                    .stream()
                    .filter(gh -> request.getDanhSachMaSKU().contains(gh.getSanPhamChiTiet().getMaSKU()))
                    .collect(Collectors.toList());
        } else {
            // Lấy tất cả sản phẩm trong giỏ
            gioHangList = gioHangDAO.findByKhachHangMaKH(request.getMaKH());
        }

        if (gioHangList.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng");
        }

        // Bước 3: Validate tồn kho và tính tổng tiền
        double tongTien = 0;
        for (GioHang gh : gioHangList) {
            SanPhamChiTiet spct = gh.getSanPhamChiTiet();
            if (spct.getSoLuong() < gh.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + spct.getMaSKU() + " không đủ số lượng trong kho. "
                        + "Kho còn: " + spct.getSoLuong() + ", yêu cầu: " + gh.getSoLuong());
            }
            tongTien += spct.getDonGia() * gh.getSoLuong();
        }

        // Bước 4: Lấy thông tin địa chỉ và chuyển thành JSON
        String diaChiJson = "";
        if (request.getMaDC() != null) {
            DiaChi diaChi = diaChiDAO.findById(request.getMaDC())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với mã: " + request.getMaDC()));

            // Chuyển địa chỉ thành JSON
            diaChiJson = createDiaChiJson(diaChi);
        }

        // Bước 5: Tạo hóa đơn (HoaDon)
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setPhuongThucTT("COD");
        hoaDon.setTrangThai("Đang xử lý");
        hoaDon.setDiaChiJson(diaChiJson);
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setNgayMua(new Date());

        hoaDon = hoaDonDAO.save(hoaDon);

        // Bước 6: Tạo chi tiết hóa đơn (HoaDonCT) và cập nhật kho
        for (GioHang gh : gioHangList) {
            SanPhamChiTiet spct = gh.getSanPhamChiTiet();

            // Tạo chi tiết hóa đơn
            HoaDonCT hoaDonCT = new HoaDonCT();
            hoaDonCT.setHoaDon(hoaDon);
            hoaDonCT.setSanPhamChiTiet(spct);
            hoaDonCT.setSoLuong(gh.getSoLuong());
            hoaDonCT.setDonGia(spct.getDonGia()); // Lưu giá tại thời điểm mua

            hoaDonCTDAO.save(hoaDonCT);

            // Cập nhật số lượng tồn kho
            spct.setSoLuong(spct.getSoLuong() - gh.getSoLuong());
            sanPhamChiTietDAO.save(spct);
        }

        // Bước 7: Xóa các sản phẩm đã mua khỏi giỏ hàng
        List<Integer> daMuaIds = gioHangList.stream()
                .map(GioHang::getMaGH)
                .collect(Collectors.toList());
        gioHangDAO.deleteAllById(daMuaIds);

        // Trả về kết quả
        return new CheckoutResponseDTO(
                hoaDon.getMaHD(),
                hoaDon.getPhuongThucTT(),
                hoaDon.getTrangThai(),
                hoaDon.getDiaChiJson(),
                tongTien,
                "Đặt hàng thành công!"
        );
    }
}
