// File: src/main/java/poly/edu/service/SanPhamService.java
package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import poly.edu.dao.SanPhamChiTietDAO;
import poly.edu.dao.SanPhamDAO;
import poly.edu.dto.SanPhamDTO;
import poly.edu.dto.SanPhamNDTO;
import poly.edu.entity.SanPham;
import poly.edu.entity.SanPhamChiTiet;
import poly.edu.entity.SanPhamDanhMuc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamDAO sanPhamDAO;

    @Autowired
    private SanPhamChiTietDAO sanPhamChiTietDAO;
 // Thêm vào SanPhamService.java
    @Autowired private poly.edu.dao.SanPhamDanhMucDAO sdmDAO;
    @Autowired private poly.edu.dao.SizeDAO sizeDAO;

    @Transactional
    public SanPham createProduct(SanPhamNDTO dto) {
        // 1. Lưu Sản Phẩm chính
        SanPham sp = new SanPham();
        sp.setTenSP(dto.getTenSP());
        sp.setMoTa(dto.getMoTa());
        sp.setGioiTinh(dto.getGioiTinh());
        sp.setKhuyenMai(dto.getKhuyenMai() != null ? dto.getKhuyenMai() : 0);
        sp.setDaBan(0);
        sp.setIsActive(true);
        SanPham savedSP = sanPhamDAO.save(sp);

        // 2. Lưu Danh mục (Bảng trung gian)
        if (dto.getCategoryIds() != null) {
            for (Integer catId : dto.getCategoryIds()) {
                SanPhamDanhMuc sdm = new SanPhamDanhMuc();
                SanPhamDanhMuc.SanPhamDanhMucId id = new SanPhamDanhMuc.SanPhamDanhMucId();
                id.setMaSP(savedSP.getMaSP());
                id.setMaDM(catId);
                sdm.setId(id);
                sdm.setSanPham(savedSP);
                sdmDAO.save(sdm);
            }
        }

        // 3. Lưu các Biến thể (Chi tiết sản phẩm)
        if (dto.getVariants() != null) {
            for (SanPhamNDTO.VariantDTO vDto : dto.getVariants()) {
                SanPhamChiTiet ct = new SanPhamChiTiet();
                ct.setSanPham(savedSP);
                ct.setTenMau(vDto.getTenMau());
                ct.setDonGia(vDto.getDonGia());
                ct.setSoLuong(vDto.getSoLuong());
                ct.setHinhAnh(vDto.getHinhAnh());
                ct.setTrangThai(vDto.getTrangThai());
                
                // Tìm đối tượng Size từ DB
                if (vDto.getMaSize() != null) {
                    ct.setSize(sizeDAO.findById(vDto.getMaSize()).orElse(null));
                }
                sanPhamChiTietDAO.save(ct);
            }
        }
        return savedSP;
    }
 // Lấy thông tin chi tiết 1 sản phẩm đổ lên Modal Sửa
    public SanPhamNDTO getProductDetail(Integer maSP) {
        SanPham sp = sanPhamDAO.findById(maSP).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        SanPhamNDTO dto = new SanPhamNDTO();
        dto.setMaSP(sp.getMaSP());
        dto.setTenSP(sp.getTenSP());
        dto.setMoTa(sp.getMoTa());
        dto.setGioiTinh(sp.getGioiTinh());
        dto.setKhuyenMai(sp.getKhuyenMai());

        // Lấy danh sách ID danh mục
        if (sp.getSanPhamDanhMucs() != null) {
            List<Integer> catIds = sp.getSanPhamDanhMucs().stream()
                    .map(sdm -> sdm.getDanhMuc().getMaDM())
                    .collect(Collectors.toList());
            dto.setCategoryIds(catIds);
        }

        // Lấy danh sách phân loại
        List<SanPhamChiTiet> chiTiets = sanPhamChiTietDAO.findBySanPham_MaSP(maSP);
        List<SanPhamNDTO.VariantDTO> variantDTOs = new ArrayList<>();
        for (SanPhamChiTiet ct : chiTiets) {
            SanPhamNDTO.VariantDTO v = new SanPhamNDTO.VariantDTO();
            v.setMaSKU(ct.getMaSKU());
            v.setTenMau(ct.getTenMau());
            v.setMaSize(ct.getSize() != null ? ct.getSize().getMaSize() : null);
            v.setDonGia(ct.getDonGia());
            v.setSoLuong(ct.getSoLuong());
            v.setHinhAnh(ct.getHinhAnh());
            v.setTrangThai(ct.getTrangThai());
            variantDTOs.add(v);
        }
        dto.setVariants(variantDTOs);
        return dto;
    }

    // Xử lý Cập nhật sản phẩm
    @Transactional
    public SanPham updateProduct(SanPhamNDTO dto) {
        SanPham sp = sanPhamDAO.findById(dto.getMaSP()).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        sp.setTenSP(dto.getTenSP());
        sp.setMoTa(dto.getMoTa());
        sp.setGioiTinh(dto.getGioiTinh());
        sp.setKhuyenMai(dto.getKhuyenMai() != null ? dto.getKhuyenMai() : 0);
        sanPhamDAO.save(sp);

        // Xóa danh mục cũ, thêm danh mục mới
        if (sp.getSanPhamDanhMucs() != null) {
            sdmDAO.deleteAll(new ArrayList<>(sp.getSanPhamDanhMucs()));
        }
        if (dto.getCategoryIds() != null) {
            for (Integer catId : dto.getCategoryIds()) {
                SanPhamDanhMuc sdm = new SanPhamDanhMuc();
                SanPhamDanhMuc.SanPhamDanhMucId id = new SanPhamDanhMuc.SanPhamDanhMucId();
                id.setMaSP(sp.getMaSP());
                id.setMaDM(catId);
                sdm.setId(id);
                sdm.setSanPham(sp);
                sdmDAO.save(sdm);
            }
        }

        // Cập nhật phân loại
        if (dto.getVariants() != null) {
            for (SanPhamNDTO.VariantDTO vDto : dto.getVariants()) {
                SanPhamChiTiet ct;
                if (vDto.getMaSKU() != null) {
                    ct = sanPhamChiTietDAO.findById(vDto.getMaSKU()).orElse(new SanPhamChiTiet());
                } else {
                    ct = new SanPhamChiTiet(); // Thêm mới nếu chưa có SKU
                }
                ct.setSanPham(sp);
                ct.setTenMau(vDto.getTenMau());
                ct.setDonGia(vDto.getDonGia());
                ct.setSoLuong(vDto.getSoLuong());
                ct.setHinhAnh(vDto.getHinhAnh());
                ct.setTrangThai(vDto.getTrangThai());
                
                if (vDto.getMaSize() != null) {
                    ct.setSize(sizeDAO.findById(vDto.getMaSize()).orElse(null));
                }
                sanPhamChiTietDAO.save(ct);
            }
        }
        return sp;
    }
    public List<SanPhamDTO> getAllProductList() {
        List<SanPham> listSP = sanPhamDAO.findAll();
        List<SanPhamDTO> result = new ArrayList<>();

        for (SanPham sp : listSP) {
            SanPhamDTO dto = new SanPhamDTO();
            dto.setMaSP(sp.getMaSP());
            dto.setTenSP(sp.getTenSP());
            dto.setGioiTinh(sp.getGioiTinh());
            dto.setIsActive(sp.getIsActive() != null ? sp.getIsActive() : false);
            
            // Lấy Khuyến mãi từ DB
            dto.setKhuyenMai(sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0);

            // 1. Lấy chuỗi Danh Mục
            if (sp.getSanPhamDanhMucs() != null) {
                String dmStr = sp.getSanPhamDanhMucs().stream()
                        .map(sdm -> sdm.getDanhMuc().getTenDM())
                        .collect(Collectors.joining(", "));
                dto.setDanhMucs(dmStr);
            }

            // 2. Tính toán số lượng, giá min/max và hình ảnh từ bảng Chi Tiết
            List<SanPhamChiTiet> chiTiets = sanPhamChiTietDAO.findBySanPham_MaSP(sp.getMaSP());
            dto.setSoPhanLoai(chiTiets.size());

            int tongTon = 0;
            Double giaMin = null;
            Double giaMax = null;
            String hinhAnh = null;

            if (!chiTiets.isEmpty()) {
                hinhAnh = chiTiets.get(0).getHinhAnh();
                for (SanPhamChiTiet ct : chiTiets) {
                    tongTon += (ct.getSoLuong() != null ? ct.getSoLuong() : 0);
                    
                    // Tìm giá thấp nhất và cao nhất
                    Double gia = ct.getDonGia();
                    if (gia != null) {
                        if (giaMin == null || gia < giaMin) giaMin = gia;
                        if (giaMax == null || gia > giaMax) giaMax = gia;
                    }
                }
            }

            dto.setTongTonKho(tongTon);
            dto.setGiaMin(giaMin != null ? giaMin : 0.0);
            dto.setGiaMax(giaMax != null ? giaMax : 0.0);
            dto.setGiaDaiDien(giaMin != null ? giaMin : 0.0); // Giữ lại giá đại diện nếu code cũ còn dùng
            dto.setHinhAnhDaiDien(hinhAnh);

            // 3. Xét trạng thái
            if (tongTon == 0) dto.setTrangThai("Hết hàng");
            else if (tongTon <= 10) dto.setTrangThai("Sắp hết");
            else dto.setTrangThai("Còn hàng");

            result.add(dto);
        }
        return result;
    }
 // Thêm hàm chuyển đổi trạng thái (Ẩn/Hiện)
    @Transactional
    public boolean toggleProductStatus(Integer maSP) {
        SanPham sp = sanPhamDAO.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        
        // Nếu isActive đang null thì coi như là true, sau đó đảo ngược lại
        boolean currentStatus = sp.getIsActive() != null ? sp.getIsActive() : true;
        sp.setIsActive(!currentStatus);
        
        sanPhamDAO.save(sp);
        return sp.getIsActive();
    }
    
}