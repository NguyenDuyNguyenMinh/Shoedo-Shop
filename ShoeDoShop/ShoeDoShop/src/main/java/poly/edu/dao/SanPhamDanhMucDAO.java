package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.SanPhamDanhMuc;
import poly.edu.entity.SanPhamDanhMuc.SanPhamDanhMucId;
import java.util.List;

public interface SanPhamDanhMucDAO extends JpaRepository<SanPhamDanhMuc, SanPhamDanhMucId> {
	
}