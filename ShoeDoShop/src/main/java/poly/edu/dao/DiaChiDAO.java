package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.DiaChi;
import java.util.List;

public interface DiaChiDAO extends JpaRepository<DiaChi, Integer> {
  
}