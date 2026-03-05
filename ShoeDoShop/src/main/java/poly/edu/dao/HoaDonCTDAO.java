package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.HoaDonCT;
import java.util.List;

public interface HoaDonCTDAO extends JpaRepository<HoaDonCT, Integer> {
	  List<HoaDonCT> findByHoaDon_MaHD(Integer maHD);
	    
	  @Modifying
	  @Query("DELETE FROM HoaDonCT ct WHERE ct.hoaDon.maHD = :maHD")
	  void deleteByHoaDonId(@Param("maHD") Integer maHD);
}