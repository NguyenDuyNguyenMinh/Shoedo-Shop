package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.QuanTri;
import poly.edu.entity.*;
import java.util.*;

public interface QuanTriDAO extends JpaRepository<QuanTri, Integer> {
    QuanTri findByUser_MaUser(Integer maUser);
    
    Optional<QuanTri> findByUser(Users user);

    List<QuanTri> findByRoleTrue();

    List<QuanTri> findByRoleFalse();

}