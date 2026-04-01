package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Voucher;
import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface VoucherDAO extends JpaRepository<Voucher, Integer> {
	@Query("SELECT v FROM Voucher v ORDER BY v.ngayBatDau DESC")
    List<Voucher> findAllOrderByNgayBatDauDesc();
	@Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.soLuong > 0 AND v.ngayBatDau <= :now AND v.ngayKetThuc >= :now")
    List<Voucher> findAvailableVouchers(@Param("now") Date now);

    List<Voucher> findByIsActiveTrue();

    List<Voucher> findByIsActiveTrueAndSoLuongGreaterThan(Integer soLuong);
}
