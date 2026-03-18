package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.DanhGiaDAO;
import poly.edu.dto.DanhGiaDTO;
import poly.edu.entity.DanhGia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DanhGiaService {

    private final DanhGiaDAO danhGiaDAO;

    // ── Lấy tất cả đánh giá của 1 sản phẩm ──────────────────────
    public List<DanhGiaDTO> layDanhGia(Integer maSP) {
        log.debug("Lay danh gia maSP={}", maSP);
        return danhGiaDAO.findBySanPhamMaSP(maSP)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Thống kê số lượng mỗi mức sao { 1:0, 2:1, 3:5 ... } ────
    public Map<Integer, Long> thongKeSao(Integer maSP) {
        Map<Integer, Long> result = new HashMap<>();
        for (int i = 1; i <= 5; i++) result.put(i, 0L);

        danhGiaDAO.findBySanPhamMaSP(maSP).forEach(dg -> {
            if (dg.getSao() != null && dg.getSao() >= 1 && dg.getSao() <= 5) {
                result.merge(dg.getSao(), 1L, Long::sum);
            }
        });
        return result;
    }

    // ── Entity → DTO ─────────────────────────────────────────────
    private DanhGiaDTO toDTO(DanhGia dg) {
        // HoaDonCT EAGER → HoaDon EAGER → KhachHang EAGER → tenKH
        String tenKH = "Khách hàng";
        try {
            tenKH = dg.getHoaDonCT()
                      .getHoaDon()
                      .getKhachHang()
                      .getTenKH();
        } catch (Exception ignored) {}

        return DanhGiaDTO.builder()
                .maDG(dg.getMaDG())
                .sao(dg.getSao())
                .danhGiaCT(dg.getDanhGiaCT())
                .ngayDG(dg.getNgayDG())
                .tenKH(tenKH)
                .build();
    }
}