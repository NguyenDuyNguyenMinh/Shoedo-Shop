package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.GioHang;
import java.util.List;
import java.util.Optional;

public interface GioHangDAO extends JpaRepository<GioHang, Integer> {
	List<GioHang> findByKhachHang_MaKH(Integer maKH);

	Optional<GioHang> findByKhachHang_MaKHAndSanPhamChiTiet_MaSKU(Integer maKH, Integer maSKU);

	@Modifying
	@Transactional
	@Query("DELETE FROM GioHang g WHERE g.khachHang.maKH = :maKH")
	void deleteByKhachHangMaKH(@Param("maKH") Integer maKH);

	@Query("SELECT COUNT(g) FROM GioHang g WHERE g.khachHang.maKH = :maKH")
	Integer countByKhachHangMaKH(@Param("maKH") Integer maKH);
}