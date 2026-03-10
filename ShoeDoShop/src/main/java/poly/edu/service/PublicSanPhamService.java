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
import poly.edu.entity.SanPhamDanhMuc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ logic trang KH_SanPham:
 *  - Lấy danh sách sản phẩm (có filter + sort)
 *  - Tìm kiếm
 *  - Lấy danh mục cho dropdown
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublicSanPhamService {

    private final PublicSanPhamDAO  publicSanPhamDAO;
    private final SanPhamChiTietDAO sanPhamChiTietDAO;
    private final DanhMucDAO        danhMucDAO;

    // ════════════════════════════════════════════════════════════
    //  Lấy danh sách tên danh mục (cho dropdown filter)
    // ════════════════════════════════════════════════════════════
    public List<String> layDanhSachDanhMuc() {
        return danhMucDAO.findAll()
                .stream()
                .map(DanhMuc::getTenDM)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════
    //  Lấy danh sách sản phẩm có filter
    //
    //  tenDM      : null / "" = tất cả danh mục
    //  gioiTinhStr: null / "Tất cả" = tất cả | "Nam" | "Nữ"
    //  onlyInStock: true = chỉ còn hàng
    //  sortBy     : "default" | "price_asc" | "price_desc" | "newest"
    // ════════════════════════════════════════════════════════════
    public List<SanPhamListDTO> laySanPhamList(
            String  tenDM,
            String  gioiTinhStr,
            boolean onlyInStock,
            String  sortBy
    ) {
        log.debug("laySanPhamList tenDM={} gioiTinh={} inStock={} sort={}",
                tenDM, gioiTinhStr, onlyInStock, sortBy);

        Boolean gioiTinh = parseGioiTinh(gioiTinhStr);
        boolean filterDM = tenDM != null && !tenDM.isBlank();

        // 1. Query DB theo filter
        List<SanPham> sanPhams = querySanPhams(filterDM ? tenDM : null, gioiTinh);

        // 2. Lấy MaSP list → batch load SKU (1 query, tránh N+1)
        List<Integer> maSPs = sanPhams.stream()
                .map(SanPham::getMaSP)
                .collect(Collectors.toList());

        Map<Integer, BigDecimal> giaMap    = new HashMap<>();
        Map<Integer, Integer>    slMap     = new HashMap<>();
        Map<Integer, String>     anhMap    = new HashMap<>();

        if (!maSPs.isEmpty()) {
            sanPhamChiTietDAO.findGiaThapNhatBatch(maSPs)
                    .forEach(row -> giaMap.put((Integer) row[0], (BigDecimal) row[1]));
            sanPhamChiTietDAO.tinhTongSoLuongBatch(maSPs)
                    .forEach(row -> slMap.put((Integer) row[0], ((Number) row[1]).intValue()));
            sanPhamChiTietDAO.findAnhDaiDienBatch(maSPs)
                    .forEach(row -> anhMap.putIfAbsent((Integer) row[0], (String) row[1]));
        }

        // 3. Map sang DTO
        List<SanPhamListDTO> dtos = sanPhams.stream()
                .map(sp -> toDTO(sp, giaMap, slMap, anhMap))
                .collect(Collectors.toList());

        // 4. Filter còn hàng
        if (onlyInStock) {
            dtos = dtos.stream()
                    .filter(SanPhamListDTO::getConHang)
                    .collect(Collectors.toList());
        }

        // 5. Sort (flash_sale và ban_chay xử lý trong switch)
        return sort(dtos, sortBy);
    }

    // ════════════════════════════════════════════════════════════
    //  Tìm kiếm theo từ khóa
    // ════════════════════════════════════════════════════════════
    public List<SanPhamListDTO> timKiem(String keyword) {
        log.debug("timKiem keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return laySanPhamList(null, null, false, "default");
        }

        String kw = keyword.trim();

        // 1. Ưu tiên tìm theo tên sản phẩm trước
        List<SanPham> sanPhams = publicSanPhamDAO.searchByTenSP(kw);

        // 2. Nếu không khớp tên SP nào → fallback tìm theo danh mục
        if (sanPhams.isEmpty()) {
            log.debug("timKiem: không khớp tên SP, fallback sang danh mục keyword={}", kw);
            sanPhams = publicSanPhamDAO.searchByDanhMuc(kw);
        }

        List<Integer> maSPs = sanPhams.stream().map(SanPham::getMaSP).collect(Collectors.toList());

        Map<Integer, BigDecimal> giaMap = new HashMap<>();
        Map<Integer, Integer>    slMap  = new HashMap<>();
        Map<Integer, String>     anhMap = new HashMap<>();

        if (!maSPs.isEmpty()) {
            sanPhamChiTietDAO.findGiaThapNhatBatch(maSPs)
                    .forEach(r -> giaMap.put((Integer) r[0], (BigDecimal) r[1]));
            sanPhamChiTietDAO.tinhTongSoLuongBatch(maSPs)
                    .forEach(r -> slMap.put((Integer) r[0], ((Number) r[1]).intValue()));
            sanPhamChiTietDAO.findAnhDaiDienBatch(maSPs)
                    .forEach(r -> anhMap.putIfAbsent((Integer) r[0], (String) r[1]));
        }

        return sanPhams.stream()
                .map(sp -> toDTO(sp, giaMap, slMap, anhMap))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════
    //  Private helpers
    // ════════════════════════════════════════════════════════════

    /** Chọn query phù hợp theo filter */
    private List<SanPham> querySanPhams(String tenDM, Boolean gioiTinh) {
        boolean filterDM = tenDM != null && !tenDM.isBlank();

        if (filterDM && gioiTinh != null)
            return publicSanPhamDAO.findByDanhMucAndGioiTinh(tenDM, gioiTinh);
        if (filterDM)
            return publicSanPhamDAO.findByDanhMucWithDetails(tenDM);
        if (gioiTinh != null)
            return publicSanPhamDAO.findByGioiTinhWithDanhMuc(gioiTinh);

        return publicSanPhamDAO.findAllActiveWithDanhMuc();
    }

    /** Map SanPham entity + pre-fetched maps → SanPhamListDTO */
    private SanPhamListDTO toDTO(
            SanPham                  sp,
            Map<Integer, BigDecimal> giaMap,
            Map<Integer, Integer>    slMap,
            Map<Integer, String>     anhMap
    ) {
        Integer maSP        = sp.getMaSP();
        int     km          = sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0;
        BigDecimal giaGoc   = giaMap.getOrDefault(maSP, BigDecimal.ZERO);
        BigDecimal giaSauKM = tinhGiaSauKM(giaGoc, km);
        int    tongSL       = slMap.getOrDefault(maSP, 0);
        String hinhAnh      = anhMap.get(maSP);

        List<String> tenDMs = sp.getSanPhamDanhMucs() == null ? List.of()
                : sp.getSanPhamDanhMucs().stream()
                        .map(spdm -> spdm.getDanhMuc().getTenDM())
                        .distinct()
                        .collect(Collectors.toList());

        return SanPhamListDTO.builder()
                .maSP(maSP)
                .tenSP(sp.getTenSP())
                .gioiTinh(sp.getGioiTinh())
                .moTa(sp.getMoTa())
                .khuyenMai(km)
                .giaGoc(km > 0 ? giaGoc : null) // chỉ gửi giá gốc khi có KM
                .giaSauKM(giaSauKM)
                .hinhAnh(hinhAnh)
                .tongSoLuong(tongSL)
                .danhMucs(tenDMs)
                .conHang(tongSL > 0)
                .daBan(sp.getDaBan() != null ? sp.getDaBan() : 0)
                .build();
    }

    /** Parse string → Boolean giới tính */
    private Boolean parseGioiTinh(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("Tất cả") || s.equalsIgnoreCase("tat ca"))
            return null;
        if (s.equalsIgnoreCase("Nam")) return true;
        if (s.equalsIgnoreCase("Nữ") || s.equalsIgnoreCase("Nu"))  return false;
        return null;
    }

    /** Tính giá sau khuyến mãi */
    private BigDecimal tinhGiaSauKM(BigDecimal giaGoc, int km) {
        if (giaGoc == null || giaGoc.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (km == 0) return giaGoc;
        return giaGoc.multiply(BigDecimal.valueOf(100 - km))
                     .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    /** Sort danh sách DTO */
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
            // Flash Sale: chỉ SP có khuyenMai > 0, sort % KM cao nhất
            case "flash_sale" -> list.stream()
                    .filter(p -> p.getKhuyenMai() != null && p.getKhuyenMai() > 0)
                    .sorted(Comparator.comparing(SanPhamListDTO::getKhuyenMai).reversed())
                    .collect(Collectors.toList());
            // Bán Chạy: sort theo daBan giảm dần
            case "ban_chay"   -> list.stream()
                    .sorted(Comparator.comparing(
                            (SanPhamListDTO p) -> p.getDaBan() != null ? p.getDaBan() : 0
                    ).reversed())
                    .collect(Collectors.toList());
            default           -> list;
        };
    }
}