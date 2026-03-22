package poly.edu.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.controller.QLHoaDonController;
import poly.edu.entity.HoaDon;
import poly.edu.entity.KhachHang;
import poly.edu.entity.QuanTri;
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

    List<HoaDon> findByKhachHang(KhachHang khachHang);
    List<HoaDon> findByQuanTri(QuanTri quanTri);

    /**
     * Tìm đơn VNPay đang chờ thanh toán đã quá hạn (timeout 15 phút).
     * Dùng cho cron job cancelExpiredVNPayOrders().
     */
    @Query("SELECT h FROM HoaDon h WHERE h.phuongThucTT = :pttt AND h.trangThai = :trangThai AND h.ngayMua <= :expiry")
    List<HoaDon> findExpiredUnpaidVNPayOrders(
            @Param("pttt") String phuongThucTT,
            @Param("trangThai") String trangThai,
            @Param("expiry") Date expiryDate);
}