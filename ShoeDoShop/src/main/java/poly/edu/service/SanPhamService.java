package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dao.SanPhamDAO;
import poly.edu.dto.SanPhamDTO;
import poly.edu.entity.SanPham;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SanPhamService {

    private final SanPhamDAO        sanPhamDAO;
    private final SanPhamChiTietDAO sanPhamChiTietDAO;

    /**
     * Tỉ lệ hiển thị trang chủ: 4 giày + 1 freesize (vớ/phụ kiện) = 5 sản phẩm/section.
     * Nếu không đủ freesize → lấp đầy bằng giày.
     */
    private static final int SO_GIAY     = 4;
    private static final int SO_FREESIZE = 1;
    private static final int TONG        = SO_GIAY + SO_FREESIZE; // 5

    // ── Flash Sales ───────────────────────────────────────────────
    public List<SanPhamDTO> layFlashSales() {
        log.debug("Lay Flash Sales");
        List<SanPham> giay     = sanPhamDAO.findFlashSalesGiay(PageRequest.of(0, SO_GIAY + 5));
        List<SanPham> freesize = sanPhamDAO.findFlashSalesFreesize(PageRequest.of(0, SO_FREESIZE + 2));
        return ghepDanhSach(giay, freesize);
    }

    // ── Noi Bat ───────────────────────────────────────────────────
    public List<SanPhamDTO> layNoiBat() {
        log.debug("Lay Noi Bat");
        List<SanPham> giay     = sanPhamDAO.findNoiBatGiay(PageRequest.of(0, SO_GIAY + 5));
        List<SanPham> freesize = sanPhamDAO.findNoiBatFreesize(PageRequest.of(0, SO_FREESIZE + 2));
        return ghepDanhSach(giay, freesize);
    }

    // ── Ban Chay ──────────────────────────────────────────────────
    public List<SanPhamDTO> layBanChay() {
        log.debug("Lay Ban Chay");
        List<SanPham> giay     = sanPhamDAO.findBanChayGiay(PageRequest.of(0, SO_GIAY + 5));
        List<SanPham> freesize = sanPhamDAO.findBanChayFreesize(PageRequest.of(0, SO_FREESIZE + 2));
        return ghepDanhSach(giay, freesize);
    }

    // ── Ghep: SO_FREESIZE freesize + phan con lai la giay ─────────
    private List<SanPhamDTO> ghepDanhSach(List<SanPham> giay, List<SanPham> freesize) {
        List<SanPham> result = new ArrayList<>(TONG);

        int soFs = Math.min(freesize.size(), SO_FREESIZE);
        result.addAll(freesize.subList(0, soFs));

        int soGiayLay = Math.min(giay.size(), TONG - soFs);
        result.addAll(giay.subList(0, soGiayLay));

        // Neu van chua du TONG, lay them giay
        if (result.size() < TONG && giay.size() > soGiayLay) {
            int them = Math.min(giay.size() - soGiayLay, TONG - result.size());
            result.addAll(giay.subList(soGiayLay, soGiayLay + them));
        }

        return result.stream().map(this::chuyenSangDTO).toList();
    }

    // ── Entity -> DTO ─────────────────────────────────────────────
    private SanPhamDTO chuyenSangDTO(SanPham sp) {
        Integer maSP = sp.getMaSP();

        BigDecimal giaGoc = sanPhamChiTietDAO
                .findGiaThapNhat(maSP)
                .orElse(BigDecimal.ZERO);

        int km = sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0;
        BigDecimal giaSauKM = giaGoc
                .multiply(BigDecimal.valueOf(100 - km))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        List<String> danhSachAnh = sanPhamChiTietDAO.findDanhSachHinhAnh(maSP);
        String hinhAnh = danhSachAnh.isEmpty() ? null : danhSachAnh.get(0);

        Integer tongSoLuong = sanPhamChiTietDAO.tinhTongSoLuong(maSP);

        return SanPhamDTO.builder()
                .maSP(maSP)
                .tenSP(sp.getTenSP())
                .hinhAnh(hinhAnh)
                .giaGoc(giaGoc)
                .giaSauKM(giaSauKM)
                .khuyenMai(km)
                .tongSoLuong(tongSoLuong != null ? tongSoLuong : 0)
                .daBan(sp.getDaBan() != null ? sp.getDaBan() : 0)
                .build();
    }
}