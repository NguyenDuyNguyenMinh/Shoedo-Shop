package poly.edu.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.controller.QLHoaDonController;
import poly.edu.entity.HoaDon;
import java.util.Date;
import java.util.List;

public interface HoaDonDAO extends JpaRepository<HoaDon, Integer> {
	List<HoaDon> findByTrangThaiOrderByNgayMuaDesc(String trangThai);
    
    @Query("SELECT h FROM HoaDon h WHERE h.trangThai IN :trangThais ORDER BY h.ngayMua DESC")
    List<HoaDon> findByTrangThaiIn(@Param("trangThais") List<String> trangThais);
    
    @Query("SELECT h FROM HoaDon h WHERE h.trangThai = :trangThai AND h.ngayMua <= :thoiGian")
    List<HoaDon> findByTrangThaiAndNgayMuaBefore(@Param("trangThai") String trangThai, @Param("thoiGian") Date thoiGian);
    
    @Modifying
    @Query("UPDATE HoaDon h SET h.trangThai = :trangThaiMoi WHERE h.maHD = :maHD")
    void updateTrangThai(@Param("maHD") Integer maHD, @Param("trangThaiMoi") String trangThaiMoi);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.maKH = :maKH")
    List<HoaDon> findHoaDonsByCustomerId(@Param("maKH") Integer maKH);
}