package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.SanPhamChiTiet;
import java.util.List;
import java.util.Optional;

public interface SanPhamChiTietDAO extends JpaRepository<SanPhamChiTiet, Integer> {
	// Thêm dòng này vào trong interface
    List<SanPhamChiTiet> findBySanPham_MaSP(Integer maSP);

	  @Modifying
	  @Query("UPDATE SanPhamChiTiet sp SET sp.soLuong = sp.soLuong - :soLuong WHERE sp.maSKU = :maSKU AND sp.soLuong >= :soLuong")
	  int truSoLuong(@Param("maSKU") Integer maSKU, @Param("soLuong") Integer soLuong);
	    
	  @Modifying
	  @Query("UPDATE SanPhamChiTiet sp SET sp.soLuong = sp.soLuong + :soLuong WHERE sp.maSKU = :maSKU")
	  int congSoLuong(@Param("maSKU") Integer maSKU, @Param("soLuong") Integer soLuong);
	    
	  @Query("SELECT sp.soLuong FROM SanPhamChiTiet sp WHERE sp.maSKU = :maSKU")
	  Integer getSoLuong(@Param("maSKU") Integer maSKU);

    @Query("SELECT MIN(sct.donGia) FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP = :maSP AND sct.soLuong > 0")
    Optional<Double> findGiaThapNhat(@Param("maSP") Integer maSP);

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

    @Query("SELECT sct.sanPham.maSP, MIN(sct.donGia) " +
           "FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP IN :maSPs AND sct.soLuong > 0 " +
           "GROUP BY sct.sanPham.maSP")
    List<Object[]> findGiaThapNhatBatch(@Param("maSPs") List<Integer> maSPs);

    @Query("SELECT sct.sanPham.maSP, SUM(sct.soLuong) " +
           "FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP IN :maSPs " +
           "GROUP BY sct.sanPham.maSP")
    List<Object[]> tinhTongSoLuongBatch(@Param("maSPs") List<Integer> maSPs);

    @Query("SELECT sct.sanPham.maSP, sct.hinhAnh " +
           "FROM SanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP IN :maSPs AND sct.soLuong > 0 " +
           "ORDER BY sct.sanPham.maSP ASC, sct.maSKU ASC")
    List<Object[]> findAnhDaiDienBatch(@Param("maSPs") List<Integer> maSPs);
}
