package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.SanPhamChiTiet;
import java.util.List;
import java.util.Optional;

public interface SanPhamChiTietDAO extends JpaRepository<SanPhamChiTiet, Integer> {
	  @Modifying
	  @Query("UPDATE SanPhamChiTiet sp SET sp.soLuong = sp.soLuong - :soLuong WHERE sp.maSKU = :maSKU AND sp.soLuong >= :soLuong")
	  int truSoLuong(@Param("maSKU") Integer maSKU, @Param("soLuong") Integer soLuong);
	    
	  @Modifying
	  @Query("UPDATE SanPhamChiTiet sp SET sp.soLuong = sp.soLuong + :soLuong WHERE sp.maSKU = :maSKU")
	  int congSoLuong(@Param("maSKU") Integer maSKU, @Param("soLuong") Integer soLuong);
	    
	  @Query("SELECT sp.soLuong FROM SanPhamChiTiet sp WHERE sp.maSKU = :maSKU")
	  Integer getSoLuong(@Param("maSKU") Integer maSKU);
}