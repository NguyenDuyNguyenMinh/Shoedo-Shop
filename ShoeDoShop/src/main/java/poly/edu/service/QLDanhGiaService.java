package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.DanhGiaDAO;
import poly.edu.dto.QLDanhGiaDTO;
import poly.edu.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QLDanhGiaService {

    @Autowired private DanhGiaDAO danhGiaDAO;

    public Map<String, Object> getAllDanhGia() {
        try {
            List<DanhGia> danhGiaList = danhGiaDAO.findAllWithDetails();
            
            // Chuyển đổi sang DTO để tránh vòng lặp JSON
            List<QLDanhGiaDTO> dtoList = danhGiaList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return success("data", dtoList);
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách đánh giá: ", e);
            // Fallback: lấy danh sách không có details
            List<DanhGia> danhGiaList = danhGiaDAO.findAll();
            List<QLDanhGiaDTO> dtoList = danhGiaList.stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
            return success("data", dtoList);
        }
    }
    
    private QLDanhGiaDTO convertToDTO(DanhGia dg) {
        QLDanhGiaDTO dto = new QLDanhGiaDTO();
        dto.setMaDG(dg.getMaDG());
        dto.setSao(dg.getSao());
        dto.setDanhGiaCT(dg.getDanhGiaCT());
        dto.setNgayDG(dg.getNgayDG());
        
        // Lấy thông tin từ HoaDonCT
        if (dg.getHoaDonCT() != null) {
            HoaDonCT hdct = dg.getHoaDonCT();
            dto.setMaHDCT(hdct.getMaHDCT());
            dto.setSoLuong(hdct.getSoLuong());
            
            // Lấy thông tin HoaDon
            if (hdct.getHoaDon() != null) {
                HoaDon hd = hdct.getHoaDon();
                dto.setMaHD(hd.getMaHD());
                dto.setPhuongThucTT(hd.getPhuongThucTT());
                dto.setTrangThaiHD(hd.getTrangThai());
                dto.setNgayMua(hd.getNgayMua());
                
                // Lấy thông tin KhachHang
                if (hd.getKhachHang() != null) {
                    KhachHang kh = hd.getKhachHang();
                    dto.setMaKH(kh.getMaKH());
                    dto.setTenKH(kh.getTenKH());
                    dto.setSdt(kh.getSdt());
                    
                    // Lấy thông tin User
                    if (kh.getUser() != null) {
                        dto.setUserName(kh.getUser().getUserName());
                        dto.setMail(kh.getUser().getMail());
                    }
                }
            }
            
            // Lấy thông tin SanPhamChiTiet
            if (hdct.getSanPhamChiTiet() != null) {
                SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
                dto.setMaSKU(spct.getMaSKU());
                dto.setTenMau(spct.getTenMau());
                dto.setHinhAnh(spct.getHinhAnh());
                
                // Lấy thông tin SanPham
                if (spct.getSanPham() != null) {
                    SanPham sp = spct.getSanPham();
                    dto.setMaSP(sp.getMaSP());
                    dto.setTenSP(sp.getTenSP());
                    dto.setGioiTinh(sp.getGioiTinh());
                }
                
                // Lấy thông tin Size
                if (spct.getSize() != null) {
                    dto.setMaSize(spct.getSize().getMaSize());
                    dto.setCoGiay(spct.getSize().getCoGiay());
                }
            }
        }
        
        return dto;
    }
    
    private QLDanhGiaDTO convertToSimpleDTO(DanhGia dg) {
    	QLDanhGiaDTO dto = new QLDanhGiaDTO();
        dto.setMaDG(dg.getMaDG());
        dto.setSao(dg.getSao());
        dto.setDanhGiaCT(dg.getDanhGiaCT());
        dto.setNgayDG(dg.getNgayDG());
        
        if (dg.getHoaDonCT() != null) {
            dto.setMaHDCT(dg.getHoaDonCT().getMaHDCT());
            dto.setTenKH("Khách hàng " + dg.getHoaDonCT().getMaHDCT());
            dto.setTenSP("Sản phẩm " + dg.getHoaDonCT().getMaHDCT());
        }
        
        return dto;
    }

    public Map<String, Object> getDanhGiaById(Integer id) {
        Optional<DanhGia> danhGia = danhGiaDAO.findById(id);
        if (danhGia.isPresent()) {
            return success("data", convertToDTO(danhGia.get()));
        }
        return error("Không tìm thấy đánh giá");
    }

    public Map<String, Object> deleteDanhGia(Integer id) {
        try {
            if (!danhGiaDAO.existsById(id)) {
                return error("Không tìm thấy đánh giá cần xóa");
            }

            Optional<DanhGia> danhGiaOpt = danhGiaDAO.findById(id);
            if (danhGiaOpt.isPresent()) {
                DanhGia danhGia = danhGiaOpt.get();
                danhGia.setDanhGiaCT("Ẩn đánh giá do vi phạm tiêu chuẩn cộng đồng");
                danhGiaDAO.save(danhGia);
                return success("Đã ẩn đánh giá thành công");
            }
            return error("Không tìm thấy đánh giá cần xóa");
            
        } catch (Exception e) {
            log.error("Lỗi khi ẩn đánh giá: ", e);
            return error("Không thể ẩn đánh giá: " + e.getMessage());
        }
    }
    
    private Map<String, Object> success(String key, Object value) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put(key, value);
        return response;
    }

    private Map<String, Object> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}