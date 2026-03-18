package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Size;
import java.util.Optional;

public interface SizeDAO extends JpaRepository<Size, Integer> {

}