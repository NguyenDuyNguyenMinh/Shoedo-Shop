package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}
