package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.HoaDon;
import java.util.Date;
import java.util.List;

public interface HoaDonDAO extends JpaRepository<HoaDon, Integer> {

}