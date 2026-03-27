package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.DanhGia;
import poly.edu.entity.HoaDonCT;
import java.util.List;
import java.util.Optional;

public interface DanhGiaDAO extends JpaRepository<DanhGia, Integer> {

    /**
     * Lấy tất cả đánh giá của 1 sản phẩm, mới nhất trước.
     * HoaDonCT.hoaDon và HoaDon.khachHang dùng EAGER nên tự load,
     * chỉ cần filter qua sanPhamChiTiet → sanPham.
     */
    @Query("SELECT dg FROM DanhGia dg " +
           "JOIN dg.hoaDonCT hdct " +
           "JOIN hdct.sanPhamChiTiet sct " +
           "WHERE sct.sanPham.maSP = :maSP " +
           "ORDER BY dg.ngayDG DESC")
    List<DanhGia> findBySanPhamMaSP(@Param("maSP") Integer maSP);

    DanhGia findByHoaDonCT(HoaDonCT hoaDonCT);

    @Query("SELECT dg FROM DanhGia dg WHERE dg.hoaDonCT.hoaDon.khachHang.maKH = :maKH")
    List<DanhGia> findByKhachHang(@Param("maKH") Integer maKH);

    @Query("SELECT dg FROM DanhGia dg WHERE dg.hoaDonCT.sanPhamChiTiet.sanPham.maSP = :maSP")
    List<DanhGia> findBySanPham(@Param("maSP") Integer maSP);
    
    //QLDanhGia
    @Query("SELECT dg FROM DanhGia dg " +
            "JOIN FETCH dg.hoaDonCT hdct " +
            "JOIN FETCH hdct.hoaDon hd " +
            "JOIN FETCH hd.khachHang kh " +
            "JOIN FETCH kh.user u " +
            "JOIN FETCH hdct.sanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "JOIN FETCH spct.size s " +
            "ORDER BY dg.ngayDG DESC")
     List<DanhGia> findAllWithDetails();

    @Query("SELECT dg FROM DanhGia dg " +
            "JOIN FETCH dg.hoaDonCT hdct " +
            "JOIN FETCH hdct.hoaDon hd " +
            "JOIN FETCH hd.khachHang kh " +
            "JOIN FETCH kh.user u " +
            "JOIN FETCH hdct.sanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "JOIN FETCH spct.size s " +
            "WHERE dg.sao = :sao " +
            "ORDER BY dg.ngayDG DESC")
     List<DanhGia> findBySao(@Param("sao") Integer sao);
     
     @Modifying
     @Transactional
     @Query(value = "DELETE FROM DanhGia WHERE MaDG = :maDG", nativeQuery = true)
     void deleteByIdNative(@Param("maDG") Integer maDG);
     
     @Modifying
     @Transactional
     @Query(value = "UPDATE HoaDonCT SET danhGia = NULL WHERE MaHDCT = (SELECT MaHDCT FROM DanhGia WHERE MaDG = :maDG)", nativeQuery = true)
     void clearDanhGiaReference(@Param("maDG") Integer maDG);
}