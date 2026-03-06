package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.GioHang;
import java.util.List;
import java.util.Optional;

public interface GioHangDAO extends JpaRepository<GioHang, Integer> {
	List<GioHang> findByKhachHang_MaKH(Integer maKH);
}