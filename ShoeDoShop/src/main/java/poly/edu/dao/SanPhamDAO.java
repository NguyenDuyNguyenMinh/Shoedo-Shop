package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.SanPham;
import java.util.List;

public interface SanPhamDAO extends JpaRepository<SanPham, Integer> {	
	@Query(value = "SELECT sp.MaSP, sp.TenSP, sp.DaBan, sp.KhuyenMai, " +
            "(SELECT TOP 1 HinhAnh FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as hinhAnh, " +
            "(SELECT MIN(DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGiaMin, " + // Lấy giá thấp nhất
            "(SELECT MAX(DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGiaMax, " + // Lấy giá cao nhất
            "sp.isActive, " + 
            "(SELECT STRING_AGG(CAST(MaDM AS VARCHAR), ',') FROM SanPham_DanhMuc sd WHERE sd.MaSP = sp.MaSP) as maDMs " +
            "FROM SanPham sp", nativeQuery = true)
	List<Object[]> getDanhSachKhuyenMaiRaw();
}