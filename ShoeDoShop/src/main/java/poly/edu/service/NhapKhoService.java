// File: src/main/java/poly/edu/service/NhapKhoService.java
package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.NhapKhoDAO;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dto.NhapKhoDTO;
import poly.edu.entity.NhapKho;
import poly.edu.entity.SanPhamChiTiet;

import java.util.Date;
import java.util.List;

@Service
public class NhapKhoService {

    @Autowired
    private NhapKhoDAO nhapKhoDAO;

    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;

    // Lấy danh sách sản phẩm để hiển thị bên tab "Nhập kho"
    public List<SanPhamChiTiet> getAllSanPhamChiTiet() {
        return sanPhamChiTietDAO.findAll();
    }

    // Lấy lịch sử nhập kho
    public List<NhapKho> getLichSuNhapKho() {
        return nhapKhoDAO.findAll(); // Có thể thêm Pageable để phân trang sau
    }

    // Xử lý nhập kho
    @Transactional
    public NhapKho thucHienNhapKho(NhapKhoDTO request) {
        // 1. Tìm sản phẩm chi tiết theo MaSKU
        SanPhamChiTiet spct = sanPhamChiTietDAO.findById(request.getMaSKU())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với SKU: " + request.getMaSKU()));

        // 2. Cập nhật số lượng tồn
        spct.setSoLuong(spct.getSoLuong() + request.getSoLuongNhap());
        sanPhamChiTietDAO.save(spct);

        // 3. Ghi log vào bảng NhapKho
        NhapKho lichSu = new NhapKho();
        lichSu.setSanPhamChiTiet(spct);
        lichSu.setSoLuong(request.getSoLuongNhap());
        lichSu.setNgayNhap(new Date());
        
        return nhapKhoDAO.save(lichSu);
    }
 // THÊM HÀM NÀY: Xử lý nhập kho hàng loạt
    @Transactional
    public List<NhapKho> thucHienNhapKhoHangLoat(List<NhapKhoDTO> requests) {
        // Dùng vòng lặp gọi lại hàm nhập đơn lẻ cho từng item
        return requests.stream()
                .map(this::thucHienNhapKho)
                .toList();
    }
}