package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.KhachHang;
import poly.edu.entity.KhachHangVoucher;
import java.util.List;
import java.util.Optional;

public interface KhachHangVoucherDAO extends JpaRepository<KhachHangVoucher, Integer> {
	List<KhachHangVoucher> findByKhachHang(KhachHang khachHang);

    /** Lấy các voucher còn hiệu lực, chưa dùng của một khách hàng */
    @Query("SELECT khv FROM KhachHangVoucher khv " +
           "JOIN FETCH khv.voucher v " +
           "WHERE khv.khachHang.maKH = :maKH " +
           "AND khv.trangThai = :trangThai " +
           "AND khv.hanSuDung >= CURRENT_TIMESTAMP " +
           "AND v.isActive = true " +
           "ORDER BY khv.hanSuDung ASC")
    List<KhachHangVoucher> findValidVouchersByMaKH(
            @Param("maKH") Integer maKH,
            @Param("trangThai") String trangThai);

    /** Tìm bản ghi KhachHang_Voucher theo MaKH_VC và đảm bảo thuộc về khách hàng */
    @Query("SELECT khv FROM KhachHangVoucher khv " +
           "JOIN FETCH khv.voucher v " +
           "WHERE khv.maKHVC = :maKHVC " +
           "AND khv.khachHang.maKH = :maKH")
    Optional<KhachHangVoucher> findByMaKHVCAndMaKH(
            @Param("maKHVC") Integer maKHVC,
            @Param("maKH")   Integer maKH);
}
