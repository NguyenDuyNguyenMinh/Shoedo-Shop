package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dao.SanPhamDAO;
import poly.edu.dto.SanPhamChiTietDTO;
import poly.edu.dto.SanPhamDetailDTO;
import poly.edu.dto.SanPhamLienQuanDTO;
import poly.edu.entity.SanPham;
import poly.edu.entity.SanPhamChiTiet;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SanPhamDetailService {

    private final SanPhamDAO        sanPhamDAO;
    private final SanPhamChiTietDAO sanPhamChiTietDAO;

    public SanPhamDetailDTO layChiTiet(Integer maSP) {
        log.debug("Lấy chi tiết sản phẩm maSP={}", maSP);

        SanPham sp = sanPhamDAO.findDetailById(maSP)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm với mã: " + maSP));

        List<SanPhamChiTiet> skus = sanPhamChiTietDAO.findAllBySanPhamMaSP(maSP);

        List<String> danhMucs = sp.getSanPhamDanhMucs() == null ? List.of()
                : sp.getSanPhamDanhMucs().stream()
                        .map(spdm -> spdm.getDanhMuc().getTenDM())
                        .distinct().collect(Collectors.toList());

        Double giaGoc = skus.stream()
                .filter(s -> s.getSoLuong() != null && s.getSoLuong() > 0)
                .map(SanPhamChiTiet::getDonGia)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(0.0);

        Double giaSauKM = tinhGiaSauKM(giaGoc, sp.getKhuyenMai());

        int tongSoLuong = skus.stream()
                .mapToInt(s -> s.getSoLuong() != null ? s.getSoLuong() : 0).sum();

        List<String> danhSachMau = skus.stream()
                .map(SanPhamChiTiet::getTenMau).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());

        List<Integer> danhSachSize = skus.stream()
                .filter(s -> s.getSize() != null && s.getSize().getCoGiay() != null)
                .map(s -> s.getSize().getCoGiay())
                .distinct().sorted().collect(Collectors.toList());

        List<String> danhSachAnh = skus.stream()
                .map(SanPhamChiTiet::getHinhAnh).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());

        List<SanPhamChiTietDTO> chiTiets = skus.stream()
                .map(sku -> SanPhamChiTietDTO.builder()
                        .maSKU(sku.getMaSKU()).tenMau(sku.getTenMau())
                        .hinhAnh(sku.getHinhAnh())
                        .coGiay(sku.getSize() != null ? sku.getSize().getCoGiay() : null)
                        .soLuong(sku.getSoLuong()).donGia(sku.getDonGia())
                        .trangThai(sku.getTrangThai()).build())
                .collect(Collectors.toList());

        return SanPhamDetailDTO.builder()
                .maSP(sp.getMaSP()).tenSP(sp.getTenSP()).moTa(sp.getMoTa())
                .gioiTinh(sp.getGioiTinh())
                .khuyenMai(sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0)
                .daBan(sp.getDaBan() != null ? sp.getDaBan() : 0)
                .danhMucs(danhMucs).giaGoc(giaGoc).giaSauKM(giaSauKM)
                .tongSoLuong(tongSoLuong).danhSachMau(danhSachMau)
                .danhSachSize(danhSachSize).danhSachHinhAnh(danhSachAnh)
                .chiTiets(chiTiets).build();
    }

    public List<SanPhamLienQuanDTO> layLienQuan(Integer maSP) {
        log.debug("Lấy sản phẩm liên quan maSP={}", maSP);

        List<SanPham> related = sanPhamDAO.findLienQuan(maSP);
        if (related.isEmpty()) return List.of();

        List<Integer> maSPs = related.stream().map(SanPham::getMaSP).collect(Collectors.toList());
        List<SanPhamChiTiet> allSkus = sanPhamChiTietDAO.findAllBySanPhamMaSPIn(maSPs);

        Map<Integer, List<SanPhamChiTiet>> skuMap = allSkus.stream()
                .collect(Collectors.groupingBy(s -> s.getSanPham().getMaSP()));

        return related.stream().map(sp -> {
            List<SanPhamChiTiet> skus = skuMap.getOrDefault(sp.getMaSP(), List.of());

            String hinhAnh = skus.stream()
                    .filter(s -> s.getSoLuong() != null && s.getSoLuong() > 0)
                    .map(SanPhamChiTiet::getHinhAnh).filter(Objects::nonNull)
                    .findFirst().orElse(null);

            Double giaGoc = skus.stream()
                    .filter(s -> s.getSoLuong() != null && s.getSoLuong() > 0)
                    .map(SanPhamChiTiet::getDonGia).filter(Objects::nonNull)
                    .min(Double::compareTo).orElse(0.0);

            Double giaSauKM = tinhGiaSauKM(giaGoc, sp.getKhuyenMai());

            int tongSL = skus.stream()
                    .mapToInt(s -> s.getSoLuong() != null ? s.getSoLuong() : 0).sum();

            String tenDM = (sp.getSanPhamDanhMucs() != null && !sp.getSanPhamDanhMucs().isEmpty())
                    ? sp.getSanPhamDanhMucs().get(0).getDanhMuc().getTenDM() : "";

            return SanPhamLienQuanDTO.builder()
                    .maSP(sp.getMaSP()).tenSP(sp.getTenSP()).hinhAnh(hinhAnh)
                    .giaGoc(giaGoc).giaSauKM(giaSauKM)
                    .khuyenMai(sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0)
                    .tongSoLuong(tongSL).daBan(sp.getDaBan() != null ? sp.getDaBan() : 0)
                    .tenDanhMuc(tenDM).build();
        }).collect(Collectors.toList());
    }

    private Double tinhGiaSauKM(Double giaGoc, Integer khuyenMai) {
        if (giaGoc == null || giaGoc == 0.0) return 0.0;
        int km = khuyenMai != null ? khuyenMai : 0;
        return giaGoc * (100 - km) / 100.0;
    }
}
