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
    //  TRANG CHỦ — Flash Sales, Nổi Bật, Bán Chạy
    // ════════════════════════════════════════════════════════════

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sp.khuyenMai > 0 AND sz.coGiay > 0 " +
           "ORDER BY sp.khuyenMai DESC")
    List<SanPham> findFlashSalesGiay(Pageable pageable);

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sp.khuyenMai > 0 AND sz.coGiay = 0 " +
           "ORDER BY sp.khuyenMai DESC")
    List<SanPham> findFlashSalesFreesize(Pageable pageable);

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay > 0 ORDER BY sp.maSP DESC")
    List<SanPham> findNoiBatGiay(Pageable pageable);

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay = 0 ORDER BY sp.maSP DESC")
    List<SanPham> findNoiBatFreesize(Pageable pageable);

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay > 0 ORDER BY sp.daBan DESC")
    List<SanPham> findBanChayGiay(Pageable pageable);

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "JOIN sp.chiTietList sct JOIN sct.size sz " +
           "WHERE sp.isActive = true AND sz.coGiay = 0 ORDER BY sp.daBan DESC")
    List<SanPham> findBanChayFreesize(Pageable pageable);

    // ════════════════════════════════════════════════════════════
    //  TRANG DETAIL
    // ════════════════════════════════════════════════════════════

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.maSP = :maSP AND sp.isActive = true")
    Optional<SanPham> findDetailById(@Param("maSP") Integer maSP);

    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.isActive = true AND sp.maSP <> :maSP " +
           "AND EXISTS (" +
           "  SELECT 1 FROM SanPhamDanhMuc lk WHERE lk.sanPham.maSP = sp.maSP " +
           "  AND lk.danhMuc.maDM IN (" +
           "    SELECT lk2.danhMuc.maDM FROM SanPhamDanhMuc lk2 WHERE lk2.sanPham.maSP = :maSP" +
           "  ))")
    List<SanPham> findLienQuan(@Param("maSP") Integer maSP);

    // ════════════════════════════════════════════════════════════
    //  QUẢN LÝ KHUYẾN MÃI (Admin)
    // ════════════════════════════════════════════════════════════

    @Query(value = "SELECT sp.MaSP, sp.TenSP, sp.DaBan, sp.KhuyenMai, " +
            "(SELECT TOP 1 HinhAnh FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as hinhAnh, " +
            "(SELECT MIN(DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGiaMin, " +
            "(SELECT MAX(DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGiaMax, " +
            "sp.isActive, " +
            "(SELECT STRING_AGG(CAST(MaDM AS VARCHAR), ',') FROM SanPham_DanhMuc sd WHERE sd.MaSP = sp.MaSP) as maDMs " +
            "FROM SanPham sp", nativeQuery = true)
    List<Object[]> getDanhSachKhuyenMaiRaw();
}
