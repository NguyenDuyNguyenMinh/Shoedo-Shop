package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.DanhGia;

import java.util.List;

@Repository
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
}