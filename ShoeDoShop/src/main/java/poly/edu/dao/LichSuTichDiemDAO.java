package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.LichSuTichDiem;
import poly.edu.entity.KhachHang;
import java.util.Date;
import java.util.List;

public interface LichSuTichDiemDAO extends JpaRepository<LichSuTichDiem, Integer> {
    
}