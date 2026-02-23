package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.MauSac;
import java.util.Optional;

public interface MauSacDAO extends JpaRepository<MauSac, Integer> {

}