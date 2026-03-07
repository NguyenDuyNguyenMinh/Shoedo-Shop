package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.DiaChi;
import java.util.List;
import java.util.Optional;

public interface DiaChiDAO extends JpaRepository<DiaChi, Integer> {

    List<DiaChi> findByKhachHangMaKH(Integer maKH);

    Optional<DiaChi> findByKhachHangMaKHAndMacDinhTrue(Integer maKH);

}