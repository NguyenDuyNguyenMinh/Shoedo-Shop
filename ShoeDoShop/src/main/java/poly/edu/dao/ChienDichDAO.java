package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.ChienDich;
import java.util.Date;
import java.util.List;
import poly.edu.dto.ChienDichDTO.CampaignResponseDTO;

public interface ChienDichDAO extends JpaRepository<ChienDich, Integer> {
	@Query("SELECT new poly.edu.dto.ChienDichDTO$CampaignResponseDTO(MIN(c.maCD), c.tenChienDich, c.thoiGianBatDau, c.thoiGianKetThuc, c.trangThai) " +
	           "FROM ChienDich c GROUP BY c.tenChienDich, c.thoiGianBatDau, c.thoiGianKetThuc, c.trangThai ORDER BY c.thoiGianBatDau DESC")
	    List<CampaignResponseDTO> getCampaignHistory();

	    List<ChienDich> findByTenChienDich(String tenChienDich);
	    
	    @Query(value = "SELECT " +
	            "sp.MaSP as maSP, " +
	            "sp.TenSP as tenSP, " +
	            "(SELECT TOP 1 ct.HinhAnh FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as hinhAnh, " +
	            "cd.KhuyenMaiCD as khuyenMai, " + // LẤY TỪ CỘT MỚI CỦA BẢNG CHIẾN DỊCH
	            "(SELECT MIN(ct.DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGiaMin, " +
	            "(SELECT MAX(ct.DonGia) FROM SanPham_ChiTiet ct WHERE ct.MaSP = sp.MaSP) as donGiaMax " +
	            "FROM ChienDich cd " +
	            "JOIN SanPham sp ON cd.MaSP = sp.MaSP " +
	            "WHERE cd.TenChienDich = :tenCD", nativeQuery = true)
	     List<Object[]> getCampaignDetailsNative(@Param("tenCD") String tenCD);
}