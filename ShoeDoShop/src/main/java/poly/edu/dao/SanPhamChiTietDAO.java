package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.SanPhamChiTiet;
import java.util.List;
import java.util.Optional;

public interface SanPhamChiTietDAO extends JpaRepository<SanPhamChiTiet, Integer> {

}