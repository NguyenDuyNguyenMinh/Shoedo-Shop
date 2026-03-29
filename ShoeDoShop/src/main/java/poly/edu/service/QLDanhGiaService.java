package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import poly.edu.dao.DanhGiaDAO;
import poly.edu.entity.DanhGia;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class QLDanhGiaService {

    @Autowired
    private DanhGiaDAO danhGiaDAO;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<DanhGia> getAllDanhGia() {
        try {
            return danhGiaDAO.findAllWithDetails();
        } catch (Exception e) {
            return danhGiaDAO.findAll();
        }
    }

    public Optional<DanhGia> getDanhGiaById(Integer maDG) {
        return danhGiaDAO.findById(maDG);
    }

    public boolean deleteDanhGia(Integer maDG) {
        
        try {
            if (!danhGiaDAO.existsById(maDG)) {
                return false;
            }

            String clearReferenceSql = "UPDATE HoaDonCT SET danhGia = NULL WHERE MaHDCT = (SELECT MaHDCT FROM DanhGia WHERE MaDG = ?)";
            try {
                jdbcTemplate.update(clearReferenceSql, maDG);
            } catch (Exception e) {
                log.warn("Không thể xóa tham chiếu (có thể đã null): {}", e.getMessage());
            }
            
            String deleteSql = "DELETE FROM DanhGia WHERE MaDG = ?";
            int rowsAffected = jdbcTemplate.update(deleteSql, maDG);
            
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
            
        } catch (Exception e) {
            return false;
        }
    }

    public List<DanhGia> getDanhGiaBySao(Integer sao) {
        try {
            return danhGiaDAO.findBySao(sao);
        } catch (Exception e) {
            return danhGiaDAO.findAll().stream()
                    .filter(dg -> dg.getSao() != null && dg.getSao().equals(sao))
                    .toList();
        }
    }

    public List<DanhGia> getDanhGiaBySanPham(Integer maSP) {
        return danhGiaDAO.findBySanPham(maSP);
    }

    public List<DanhGia> getDanhGiaByKhachHang(Integer maKH) {
        return danhGiaDAO.findByKhachHang(maKH);
    }
}