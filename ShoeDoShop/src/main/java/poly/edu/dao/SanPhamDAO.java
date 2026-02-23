package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.SanPham;
import java.util.List;

public interface SanPhamDAO extends JpaRepository<SanPham, Integer> {

}