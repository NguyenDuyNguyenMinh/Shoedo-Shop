package poly.edu.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.SanPham;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamDAO extends JpaRepository<SanPham, Integer> {

    // ════════════════════════════════════════════════════════════
    //  TRANG CHỦ — 3 section chính
    //  Mỗi section có 2 query: chỉ giày (CoGiay > 0) và chỉ freesize (CoGiay = 0)
    //  Service sẽ ghép theo tỉ lệ 4 giày : 1 freesize
    // ════════════════════════════════════════════════════════════

    // ── Flash Sales ──────────────────────────────────────────────

    /** Giày có khuyến mãi (CoGiay > 0 = có số size cụ thể) */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct " +
           "JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sp.khuyenMai > 0 AND sz.coGiay > 0 " +
           "ORDER BY sp.khuyenMai DESC")
    List<SanPham> findFlashSalesGiay(Pageable pageable);

    /** Freesize có khuyến mãi (CoGiay = 0 = vớ, phụ kiện) */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct " +
           "JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sp.khuyenMai > 0 AND sz.coGiay = 0 " +
           "ORDER BY sp.khuyenMai DESC")
    List<SanPham> findFlashSalesFreesize(Pageable pageable);

    // ── Nổi Bật ──────────────────────────────────────────────────

    /** Giày mới nhất */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct " +
           "JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay > 0 " +
           "ORDER BY sp.maSP DESC")
    List<SanPham> findNoiBatGiay(Pageable pageable);

    /** Freesize mới nhất */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct " +
           "JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay = 0 " +
           "ORDER BY sp.maSP DESC")
    List<SanPham> findNoiBatFreesize(Pageable pageable);

    // ── Bán Chạy ─────────────────────────────────────────────────

    /** Giày bán nhiều nhất */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct " +
           "JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay > 0 " +
           "ORDER BY sp.daBan DESC")
    List<SanPham> findBanChayGiay(Pageable pageable);

    /** Freesize bán nhiều nhất */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct " +
           "JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay = 0 " +
           "ORDER BY sp.daBan DESC")
    List<SanPham> findBanChayFreesize(Pageable pageable);

    // ════════════════════════════════════════════════════════════
    //  TRANG DETAIL
    // ════════════════════════════════════════════════════════════

    /** Lấy 1 sản phẩm active theo ID, kèm danh mục */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.maSP = :maSP AND sp.isActive = true")
    Optional<SanPham> findDetailById(@Param("maSP") Integer maSP);

    /** Sản phẩm liên quan (cùng danh mục, khác ID) */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.isActive = true " +
           "AND sp.maSP <> :maSP " +
           "AND EXISTS (" +
           "  SELECT 1 FROM SanPhamDanhMuc lk " +
           "  WHERE lk.sanPham.maSP = sp.maSP " +
           "  AND lk.danhMuc.maDM IN (" +
           "    SELECT lk2.danhMuc.maDM FROM SanPhamDanhMuc lk2 " +
           "    WHERE lk2.sanPham.maSP = :maSP" +
           "  )" +
           ")")
    List<SanPham> findLienQuan(@Param("maSP") Integer maSP);
}