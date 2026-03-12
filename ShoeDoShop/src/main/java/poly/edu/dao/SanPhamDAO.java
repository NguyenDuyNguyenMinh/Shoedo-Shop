package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.SanPham;
import java.util.List;

public interface SanPhamDAO extends JpaRepository<SanPham, Integer> {	
	@Query(value = "SELECT sp.MaSP, sp.TenSP, sp.DaBan, sp.KhuyenMai, " +
            "(SELECT TOP 1 HinhAnh FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as hinhAnh, " +
            "(SELECT MIN(DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGia," + "sp.isActive " +
            "FROM SanPham sp", nativeQuery = true)
List<Object[]> getDanhSachKhuyenMaiRaw();
}