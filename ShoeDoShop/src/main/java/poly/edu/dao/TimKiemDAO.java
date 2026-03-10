package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.TimKiem;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimKiemDAO extends JpaRepository<TimKiem, Integer> {

    /**
     * Lấy lịch sử tìm kiếm của 1 khách hàng, mới nhất trước, tối đa 10 từ khóa.
     */
    @Query("SELECT tk FROM TimKiem tk " +
           "WHERE tk.khachHang.maKH = :maKH " +
           "ORDER BY tk.thoiGian DESC")
    List<TimKiem> findTop10ByKhachHangMaKH(@Param("maKH") Integer maKH);

    /**
     * Tìm bản ghi theo MaKH + từ khóa (kiểm tra duplicate trước khi lưu).
     */
    @Query("SELECT tk FROM TimKiem tk " +
           "WHERE tk.khachHang.maKH = :maKH " +
           "AND LOWER(tk.noiDungTimKiem) = LOWER(:keyword)")
    Optional<TimKiem> findByMaKHAndKeyword(
            @Param("maKH") Integer maKH,
            @Param("keyword") String keyword);

    /**
     * Xóa toàn bộ lịch sử tìm kiếm của 1 khách hàng.
     */
    @Modifying
    @Query("DELETE FROM TimKiem tk WHERE tk.khachHang.maKH = :maKH")
    void deleteAllByMaKH(@Param("maKH") Integer maKH);

    /**
     * Xóa 1 từ khóa cụ thể.
     */
    @Modifying
    @Query("DELETE FROM TimKiem tk " +
           "WHERE tk.khachHang.maKH = :maKH " +
           "AND LOWER(tk.noiDungTimKiem) = LOWER(:keyword)")
    void deleteByMaKHAndKeyword(
            @Param("maKH") Integer maKH,
            @Param("keyword") String keyword);
}