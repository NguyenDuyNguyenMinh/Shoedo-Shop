package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.DanhMucDAO;
import poly.edu.dao.PublicSanPhamDAO;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dto.SanPhamListDTO;
import poly.edu.entity.DanhMuc;
import poly.edu.entity.SanPham;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicSanPhamService {

    private final PublicSanPhamDAO  publicSanPhamDAO;
    private final SanPhamChiTietDAO sanPhamChiTietDAO;
    private final DanhMucDAO        danhMucDAO;

    public List<String> layDanhSachDanhMuc() {
        return danhMucDAO.findAll().stream()
                .map(DanhMuc::getTenDM)
                .collect(Collectors.toList());
    }

    public List<SanPhamListDTO> laySanPhamList(
            String tenDM, String gioiTinhStr, boolean onlyInStock, String sortBy) {
        log.debug("laySanPhamList tenDM={} gioiTinh={} inStock={} sort={}", tenDM, gioiTinhStr, onlyInStock, sortBy);

        Boolean gioiTinh = parseGioiTinh(gioiTinhStr);
        boolean filterDM = tenDM != null && !tenDM.isBlank();

        List<SanPham> sanPhams = querySanPhams(filterDM ? tenDM : null, gioiTinh);
        List<Integer> maSPs = sanPhams.stream().map(SanPham::getMaSP).collect(Collectors.toList());

        Map<Integer, Double>  giaMap = new HashMap<>();
        Map<Integer, Integer> slMap  = new HashMap<>();
        Map<Integer, String>  anhMap = new HashMap<>();

        if (!maSPs.isEmpty()) {
            sanPhamChiTietDAO.findGiaThapNhatBatch(maSPs)
                    .forEach(row -> giaMap.put((Integer) row[0], ((Number) row[1]).doubleValue()));
            sanPhamChiTietDAO.tinhTongSoLuongBatch(maSPs)
                    .forEach(row -> slMap.put((Integer) row[0], ((Number) row[1]).intValue()));
            sanPhamChiTietDAO.findAnhDaiDienBatch(maSPs)
                    .forEach(row -> anhMap.putIfAbsent((Integer) row[0], (String) row[1]));
        }

        List<SanPhamListDTO> dtos = sanPhams.stream()
                .map(sp -> toDTO(sp, giaMap, slMap, anhMap))
                .collect(Collectors.toList());

        if (onlyInStock) {
            dtos = dtos.stream().filter(SanPhamListDTO::getConHang).collect(Collectors.toList());
        }

        return sort(dtos, sortBy);
    }

    public List<SanPhamListDTO> timKiem(String keyword) {
        log.debug("timKiem keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return laySanPhamList(null, null, false, "default");
        }

        String kw = keyword.trim();
        List<SanPham> sanPhams = publicSanPhamDAO.searchByTenSP(kw);
        if (sanPhams.isEmpty()) {
            log.debug("timKiem: không khớp tên SP, fallback sang danh mục keyword={}", kw);
            sanPhams = publicSanPhamDAO.searchByDanhMuc(kw);
        }

        List<Integer> maSPs = sanPhams.stream().map(SanPham::getMaSP).collect(Collectors.toList());

        Map<Integer, Double>  giaMap = new HashMap<>();
        Map<Integer, Integer> slMap  = new HashMap<>();
        Map<Integer, String>  anhMap = new HashMap<>();

        if (!maSPs.isEmpty()) {
            sanPhamChiTietDAO.findGiaThapNhatBatch(maSPs)
                    .forEach(r -> giaMap.put((Integer) r[0], ((Number) r[1]).doubleValue()));
            sanPhamChiTietDAO.tinhTongSoLuongBatch(maSPs)
                    .forEach(r -> slMap.put((Integer) r[0], ((Number) r[1]).intValue()));
            sanPhamChiTietDAO.findAnhDaiDienBatch(maSPs)
                    .forEach(r -> anhMap.putIfAbsent((Integer) r[0], (String) r[1]));
        }

        return sanPhams.stream()
                .map(sp -> toDTO(sp, giaMap, slMap, anhMap))
                .collect(Collectors.toList());
    }

    private List<SanPham> querySanPhams(String tenDM, Boolean gioiTinh) {
        boolean filterDM = tenDM != null && !tenDM.isBlank();
        if (filterDM && gioiTinh != null) return publicSanPhamDAO.findByDanhMucAndGioiTinh(tenDM, gioiTinh);
        if (filterDM) return publicSanPhamDAO.findByDanhMucWithDetails(tenDM);
        if (gioiTinh != null) return publicSanPhamDAO.findByGioiTinhWithDanhMuc(gioiTinh);
        return publicSanPhamDAO.findAllActiveWithDanhMuc();
    }

    private SanPhamListDTO toDTO(
            SanPham sp,
            Map<Integer, Double>  giaMap,
            Map<Integer, Integer> slMap,
            Map<Integer, String>  anhMap) {
        Integer maSP   = sp.getMaSP();
        int     km     = sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0;
        Double giaGoc  = giaMap.getOrDefault(maSP, 0.0);
        Double giaSauKM = tinhGiaSauKM(giaGoc, km);
        int    tongSL  = slMap.getOrDefault(maSP, 0);
        String hinhAnh = anhMap.get(maSP);

        List<String> tenDMs = sp.getSanPhamDanhMucs() == null ? List.of()
                : sp.getSanPhamDanhMucs().stream()
                        .map(spdm -> spdm.getDanhMuc().getTenDM())
                        .distinct().collect(Collectors.toList());

        return SanPhamListDTO.builder()
                .maSP(maSP).tenSP(sp.getTenSP()).gioiTinh(sp.getGioiTinh())
                .moTa(sp.getMoTa()).khuyenMai(km)
                .giaGoc(km > 0 ? giaGoc : null)
                .giaSauKM(giaSauKM).hinhAnh(hinhAnh)
                .tongSoLuong(tongSL).danhMucs(tenDMs)
                .conHang(tongSL > 0)
                .daBan(sp.getDaBan() != null ? sp.getDaBan() : 0)
                .build();
    }

    private Boolean parseGioiTinh(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("Tất cả") || s.equalsIgnoreCase("tat ca")) return null;
        if (s.equalsIgnoreCase("Nam")) return true;
        if (s.equalsIgnoreCase("Nữ") || s.equalsIgnoreCase("Nu")) return false;
        return null;
    }

    private Double tinhGiaSauKM(Double giaGoc, int km) {
        if (giaGoc == null || giaGoc == 0.0) return 0.0;
        if (km == 0) return giaGoc;
        return giaGoc * (100 - km) / 100.0;
    }

    private List<SanPhamListDTO> sort(List<SanPhamListDTO> list, String sortBy) {
        if (sortBy == null) return list;
        return switch (sortBy) {
            case "price_asc"  -> list.stream()
                    .sorted(Comparator.comparing(SanPhamListDTO::getGiaSauKM))
                    .collect(Collectors.toList());
            case "price_desc" -> list.stream()
                    .sorted(Comparator.comparing(SanPhamListDTO::getGiaSauKM).reversed())
                    .collect(Collectors.toList());
            case "newest"     -> list.stream()
                    .sorted(Comparator.comparing(SanPhamListDTO::getMaSP).reversed())
                    .collect(Collectors.toList());
            case "flash_sale" -> list.stream()
                    .filter(p -> p.getKhuyenMai() != null && p.getKhuyenMai() > 0)
                    .sorted(Comparator.comparing(SanPhamListDTO::getKhuyenMai).reversed())
                    .collect(Collectors.toList());
            case "ban_chay"   -> list.stream()
                    .sorted(Comparator.comparing(
                            (SanPhamListDTO p) -> p.getDaBan() != null ? p.getDaBan() : 0
                    ).reversed()).collect(Collectors.toList());
            default           -> list;
        };
    }
}
