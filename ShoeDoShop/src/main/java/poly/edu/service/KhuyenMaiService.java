package poly.edu.service;

import poly.edu.dto.KhuyenMaiUpdateRequest;
import poly.edu.entity.SanPham;
import poly.edu.dao.SanPhamDAO;
import poly.edu.dto.SanPhamKhuyenMaiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal; 
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KhuyenMaiService {

    @Autowired
    private SanPhamDAO sanPhamRepository;

    public List<SanPhamKhuyenMaiResponse> getDanhSachKhuyenMai() {
        List<Object[]> rawData = sanPhamRepository.getDanhSachKhuyenMaiRaw();
        
        return rawData.stream().map(row -> new SanPhamKhuyenMaiResponse(
            (Integer) row[0],  // maSP
            (String) row[1],   // tenSP
            (Integer) row[2],  // daBan
            (Integer) row[3],  // khuyenMai
            (String) row[4],   // hinhAnh
            row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO, // donGiaMin
            	    row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO,
            (Boolean) row[7],
            row[8] != null ? row[7].toString() : ""
        )).collect(Collectors.toList());
    }

    // Cập nhật 1 sản phẩm
    @Transactional
    public void capNhatKhuyenMai(KhuyenMaiUpdateRequest request) {
        if (request.getKhuyenMai() < 0 || request.getKhuyenMai() > 100) {
            throw new IllegalArgumentException("Khuyến mãi phải từ 0 đến 100%");
        }

        SanPham sp = sanPhamRepository.findById(request.getMaSP())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Sản phẩm với mã: " + request.getMaSP()));
        
        sp.setKhuyenMai(request.getKhuyenMai());
        sanPhamRepository.save(sp);
    }

    // Cập nhật hàng loạt (Bulk Update)
    @Transactional
    public void capNhatKhuyenMaiHangLoat(List<KhuyenMaiUpdateRequest> requests) {
        for (KhuyenMaiUpdateRequest req : requests) {
            capNhatKhuyenMai(req); // Tái sử dụng logic validate và update ở trên
        }
    }
}