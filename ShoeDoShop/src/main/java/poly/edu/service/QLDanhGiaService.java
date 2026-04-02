package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import poly.edu.dao.DanhGiaDAO;
import poly.edu.entity.DanhGia;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class QLDanhGiaService {

    @Autowired private DanhGiaDAO danhGiaDAO;
    @Autowired private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getAllDanhGia() {
        try {
            List<DanhGia> danhGiaList = danhGiaDAO.findAllWithDetails();
            return success("data", danhGiaList);
        } catch (Exception e) {
            return success("data", danhGiaDAO.findAll());
        }
    }

    public Map<String, Object> getDanhGiaById(Integer id) {
        Optional<DanhGia> danhGia = danhGiaDAO.findById(id);
        if (danhGia.isPresent()) {
            return success("data", danhGia.get());
        }
        return error("Không tìm thấy đánh giá");
    }

    public Map<String, Object> deleteDanhGia(Integer id) {
        try {
            if (!danhGiaDAO.existsById(id)) {
                return error("Không tìm thấy đánh giá cần xóa");
            }
            
            danhGiaDAO.deleteByIdNative(id);
            return success("Xóa đánh giá thành công");
            
        } catch (Exception e) {
            return error("Không thể xóa đánh giá: " + e.getMessage());
        }
    }

    private Map<String, Object> success(String key, Object value) {
        return Map.of("success", true, key, value);
    }

    private Map<String, Object> success(String message) {
        return Map.of("success", true, "message", message);
    }

    private Map<String, Object> error(String message) {
        return Map.of("success", false, "message", message);
    }
}