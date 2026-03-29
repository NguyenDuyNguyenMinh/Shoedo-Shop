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

    public List<SanPhamChiTiet> getAllSanPhamChiTiet() {
        return sanPhamChiTietDAO.findAll();
    }

    public List<NhapKho> getLichSuNhapKho() {
        return nhapKhoDAO.findAll();
    }

    @Transactional
    public NhapKho thucHienNhapKho(NhapKhoDTO request) {
        SanPhamChiTiet spct = sanPhamChiTietDAO.findById(request.getMaSKU())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với SKU: " + request.getMaSKU()));
        spct.setSoLuong(spct.getSoLuong() + request.getSoLuongNhap());
        sanPhamChiTietDAO.save(spct);
        NhapKho lichSu = new NhapKho();
        lichSu.setSanPhamChiTiet(spct);
        lichSu.setSoLuong(request.getSoLuongNhap());
        lichSu.setNgayNhap(new Date());
        
        return nhapKhoDAO.save(lichSu);
    }

    @Transactional
    public List<NhapKho> thucHienNhapKhoHangLoat(List<NhapKhoDTO> requests) {
        return requests.stream()
                .map(this::thucHienNhapKho)
                .toList();
    }
}