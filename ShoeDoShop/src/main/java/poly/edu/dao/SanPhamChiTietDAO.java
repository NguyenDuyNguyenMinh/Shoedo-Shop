package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.SanPhamChiTiet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DAO cho SanPham_ChiTiet.
 *
 * Bao gồm:
 *  - Các query đơn lẻ (dùng cho trang chủ / trang detail 1 SP)
 *  - Các query BATCH (dùng cho trang danh sách — tránh N+1)
 */
@Repository
public interface SanPhamChiTietDAO extends JpaRepository<SanPhamChiTiet, Integer> {

    // ════════════════════════════════════════════════════════════
    //  QUERY ĐƠN LẺ — trang chủ / trang detail
    // ════════════════════════════════════════════════════════════

    @Query("SELECT MIN(sct.donGia) FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP = :maSP AND sct.soLuong > 0")
    Optional<BigDecimal> findGiaThapNhat(@Param("maSP") Integer maSP);

    @Query("SELECT COALESCE(SUM(sct.soLuong), 0) FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP = :maSP")
    Integer tinhTongSoLuong(@Param("maSP") Integer maSP);

    @Query("SELECT sct.hinhAnh FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP = :maSP AND sct.soLuong > 0 " +
           "ORDER BY sct.maSKU ASC")
    List<String> findDanhSachHinhAnh(@Param("maSP") Integer maSP);

    @Query("SELECT sct FROM SanPhamChiTiet sct " +
           "JOIN FETCH sct.size " +
           "WHERE sct.sanPham.maSP = :maSP " +
           "ORDER BY sct.maSKU ASC")
    List<SanPhamChiTiet> findAllBySanPhamMaSP(@Param("maSP") Integer maSP);

    @Query("SELECT sct FROM SanPhamChiTiet sct " +
           "JOIN FETCH sct.size " +
           "WHERE sct.sanPham.maSP IN :maSPs " +
           "ORDER BY sct.sanPham.maSP ASC, sct.maSKU ASC")
    List<SanPhamChiTiet> findAllBySanPhamMaSPIn(@Param("maSPs") List<Integer> maSPs);

    // ════════════════════════════════════════════════════════════
    //  QUERY BATCH — trang KH_SanPham (N sản phẩm cùng lúc)
    //  Trả về Object[] để tránh tạo thêm DTO trung gian
    // ════════════════════════════════════════════════════════════

    /**
     * Giá thấp nhất (SKU còn hàng) cho nhiều sản phẩm cùng lúc.
     * Trả về: Object[] { maSP (Integer), giaThapNhat (BigDecimal) }
     */
    @Query("SELECT sct.sanPham.maSP, MIN(sct.donGia) " +
           "FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP IN :maSPs AND sct.soLuong > 0 " +
           "GROUP BY sct.sanPham.maSP")
    List<Object[]> findGiaThapNhatBatch(@Param("maSPs") List<Integer> maSPs);

    /**
     * Tổng số lượng tồn kho cho nhiều sản phẩm cùng lúc.
     * Trả về: Object[] { maSP (Integer), tongSoLuong (Long) }
     */
    @Query("SELECT sct.sanPham.maSP, SUM(sct.soLuong) " +
           "FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP IN :maSPs " +
           "GROUP BY sct.sanPham.maSP")
    List<Object[]> tinhTongSoLuongBatch(@Param("maSPs") List<Integer> maSPs);

    /**
     * Ảnh đại diện (SKU đầu tiên còn hàng) cho nhiều sản phẩm cùng lúc.
     * Trả về: Object[] { maSP (Integer), hinhAnh (String) }
     * Lưu ý: dùng putIfAbsent để chỉ giữ ảnh đầu tiên mỗi SP.
     */
    @Query("SELECT sct.sanPham.maSP, sct.hinhAnh " +
           "FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP IN :maSPs AND sct.soLuong > 0 " +
           "ORDER BY sct.sanPham.maSP ASC, sct.maSKU ASC")
    List<Object[]> findAnhDaiDienBatch(@Param("maSPs") List<Integer> maSPs);

    // ════════════════════════════════════════════════════════════
    //  CẬP NHẬT SỐ LƯỢNG — đặt hàng / nhập kho
    // ════════════════════════════════════════════════════════════

    @Modifying
    @Query("UPDATE SanPhamChiTiet sct " +
           "SET sct.soLuong = GREATEST(0, sct.soLuong - :soLuong) " +
           "WHERE sct.maSKU = :maSKU")
    void truSoLuong(@Param("maSKU") Integer maSKU, @Param("soLuong") Integer soLuong);

    @Modifying
    @Query("UPDATE SanPhamChiTiet sct " +
           "SET sct.soLuong = sct.soLuong + :soLuong " +
           "WHERE sct.maSKU = :maSKU")
    void congSoLuong(@Param("maSKU") Integer maSKU, @Param("soLuong") Integer soLuong);
}