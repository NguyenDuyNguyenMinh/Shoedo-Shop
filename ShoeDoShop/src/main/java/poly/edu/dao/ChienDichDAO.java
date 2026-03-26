package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.ChienDich;
import java.util.Date;
import java.util.List;

public interface ChienDichDAO extends JpaRepository<ChienDich, Integer> {

}