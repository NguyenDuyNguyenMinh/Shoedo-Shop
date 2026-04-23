package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.DanhGiaDAO;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QLDanhGiaService {

    @Autowired private DanhGiaDAO danhGiaDAO;

    public Map<String, Object> getAllDanhGia() {
        try {
            List<Map<String, Object>> data = danhGiaDAO.findAllWithDetails().stream().map(dg -> {
                Map<String, Object> m = new HashMap<>();
                m.put("maDG", dg.getMaDG());
                m.put("sao", dg.getSao());
                m.put("danhGiaCT", dg.getDanhGiaCT());
                m.put("ngayDG", dg.getNgayDG());
                try {
                    m.put("tenKH", dg.getHoaDonCT().getHoaDon().getKhachHang().getTenKH());
                    m.put("userName", dg.getHoaDonCT().getHoaDon().getKhachHang().getUser().getUserName());
                    m.put("tenSP", dg.getHoaDonCT().getSanPhamChiTiet().getSanPham().getTenSP());
                    m.put("tenMau", dg.getHoaDonCT().getSanPhamChiTiet().getTenMau());
                    m.put("coGiay", dg.getHoaDonCT().getSanPhamChiTiet().getSize().getCoGiay());
                } catch (Exception ignored) {}
                return m;
            }).collect(Collectors.toList());
            return res(true, "data", data);
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách đánh giá: ", e);
            return res(false, "message", "Không thể tải danh sách đánh giá");
        }
    }

    public Map<String, Object> getDanhGiaById(Integer id) {
        return danhGiaDAO.findById(id).map(dg -> {
            Map<String, Object> m = new HashMap<>();
            m.put("maDG", dg.getMaDG());
            m.put("sao", dg.getSao());
            m.put("danhGiaCT", dg.getDanhGiaCT());
            m.put("ngayDG", dg.getNgayDG());
            try {
                m.put("tenKH", dg.getHoaDonCT().getHoaDon().getKhachHang().getTenKH());
                m.put("userName", dg.getHoaDonCT().getHoaDon().getKhachHang().getUser().getUserName());
                m.put("tenSP", dg.getHoaDonCT().getSanPhamChiTiet().getSanPham().getTenSP());
                m.put("tenMau", dg.getHoaDonCT().getSanPhamChiTiet().getTenMau());
                m.put("coGiay", dg.getHoaDonCT().getSanPhamChiTiet().getSize().getCoGiay());
            } catch (Exception ignored) {}
            return res(true, "data", m);
        }).orElseGet(() -> res(false, "message", "Không tìm thấy đánh giá"));
    }

    public Map<String, Object> deleteDanhGia(Integer id) {
        return danhGiaDAO.findById(id).map(dg -> {
            dg.setDanhGiaCT("Ẩn đánh giá do vi phạm tiêu chuẩn cộng đồng");
            danhGiaDAO.save(dg);
            return res(true, "message", "Đã ẩn đánh giá thành công");
        }).orElseGet(() -> res(false, "message", "Không tìm thấy đánh giá cần xóa"));
    }
    
    private Map<String, Object> res(boolean success, String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", success);
        m.put(key, value);
        return m;
    }
}