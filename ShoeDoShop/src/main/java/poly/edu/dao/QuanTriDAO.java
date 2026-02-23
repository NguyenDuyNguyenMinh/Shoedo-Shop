package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.QuanTri;
import poly.edu.entity.*;
import java.util.*;

public interface QuanTriDAO extends JpaRepository<QuanTri, Integer> {
	// Tìm theo MaUser
    QuanTri findByUser_MaUser(Integer maUser);
    
    // Tìm theo user entity
    Optional<QuanTri> findByUser(Users user);
    
    // Tìm tất cả admin
    List<QuanTri> findByRoleTrue();
    
    // Tìm tất cả nhân viên
    List<QuanTri> findByRoleFalse();
}