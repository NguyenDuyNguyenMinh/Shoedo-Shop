package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.TimKiem;
import java.util.List;

public interface TimKiemDAO extends JpaRepository<TimKiem, Integer> {
  
}