package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.SanPham;

import java.util.List;
import java.util.Optional;

/**
 * DAO dành riêng cho trang KH_SanPham (danh sách + chi tiết công khai).
 *
 * Tất cả query đều DISTINCT + filter isActive = true.
 * Dùng JOIN FETCH để tránh N+1 problem.
 */
@Repository
public interface PublicSanPhamDAO extends JpaRepository<SanPham, Integer> {

    // ════════════════════════════════════════════════════════════
    //  TRANG DANH SÁCH — KH_SanPham
    // ════════════════════════════════════════════════════════════

    /**
     * Lấy tất cả sản phẩm active, kèm danh mục.
     * SKU sẽ load riêng qua SanPhamChiTietDAO để tránh cartesian join.
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.isActive = true " +
           "ORDER BY sp.maSP ASC")
    List<SanPham> findAllActiveWithDanhMuc();

    /**
     * Lọc theo giới tính (true=Nam, false=Nữ).
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.isActive = true " +
           "AND sp.gioiTinh = :gioiTinh " +
           "ORDER BY sp.maSP ASC")
    List<SanPham> findByGioiTinhWithDanhMuc(@Param("gioiTinh") Boolean gioiTinh);

    /**
     * Lọc theo tên danh mục.
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc dm " +
           "WHERE sp.isActive = true " +
           "AND dm.tenDM = :tenDM " +
           "ORDER BY sp.maSP ASC")
    List<SanPham> findByDanhMucWithDetails(@Param("tenDM") String tenDM);

    /**
     * Lọc theo tên danh mục + giới tính.
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc dm " +
           "WHERE sp.isActive = true " +
           "AND dm.tenDM = :tenDM " +
           "AND sp.gioiTinh = :gioiTinh " +
           "ORDER BY sp.maSP ASC")
    List<SanPham> findByDanhMucAndGioiTinh(
            @Param("tenDM")    String  tenDM,
            @Param("gioiTinh") Boolean gioiTinh);

    /**
     * Tìm kiếm theo tên sản phẩm (không phân biệt hoa thường).
     * Ưu tiên dùng cái này trước khi fallback sang danh mục.
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.isActive = true " +
           "AND LOWER(sp.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY sp.maSP ASC")
    List<SanPham> searchByTenSP(@Param("keyword") String keyword);

    /**
     * Fallback: tìm tất cả SP thuộc danh mục có tên khớp keyword.
     * Chỉ dùng khi searchByTenSP trả về rỗng.
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc dm " +
           "WHERE sp.isActive = true " +
           "AND LOWER(dm.tenDM) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY sp.maSP ASC")
    List<SanPham> searchByDanhMuc(@Param("keyword") String keyword);

    // ════════════════════════════════════════════════════════════
    //  TRANG DETAIL — dùng chung với SanPhamDAO nếu cần
    // ════════════════════════════════════════════════════════════

    /**
     * Lấy 1 sản phẩm active theo ID, kèm danh mục.
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
           "LEFT JOIN FETCH sp.sanPhamDanhMucs spdm " +
           "LEFT JOIN FETCH spdm.danhMuc " +
           "WHERE sp.maSP = :maSP AND sp.isActive = true")
    Optional<SanPham> findDetailById(@Param("maSP") Integer maSP);

    /**
     * Sản phẩm liên quan (cùng ít nhất 1 danh mục, khác ID hiện tại).
     */
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