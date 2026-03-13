package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
@Slf4j
public class SanPhamService {

    @Autowired private SanPhamDAO sanPhamDAO;
    @Autowired private SanPhamChiTietDAO sanPhamChiTietDAO;
    @Autowired private poly.edu.dao.SanPhamDanhMucDAO sdmDAO;
    @Autowired private poly.edu.dao.SizeDAO sizeDAO;

    private static final int SO_GIAY     = 4;
    private static final int SO_FREESIZE = 1;
    private static final int TONG        = SO_GIAY + SO_FREESIZE;

    // ══════════════════════════════════════════════════════════════
    //  TRANG CHỦ — Flash Sales, Nổi Bật, Bán Chạy
    // ══════════════════════════════════════════════════════════════

    public List<SanPhamDTO> layFlashSales() {
        log.debug("Lay Flash Sales");
        return ghepDanhSach(
            sanPhamDAO.findFlashSalesGiay(PageRequest.of(0, SO_GIAY + 5)),
            sanPhamDAO.findFlashSalesFreesize(PageRequest.of(0, SO_FREESIZE + 2))
        );
    }

    public List<SanPhamDTO> layNoiBat() {
        log.debug("Lay Noi Bat");
        return ghepDanhSach(
            sanPhamDAO.findNoiBatGiay(PageRequest.of(0, SO_GIAY + 5)),
            sanPhamDAO.findNoiBatFreesize(PageRequest.of(0, SO_FREESIZE + 2))
        );
    }

    public List<SanPhamDTO> layBanChay() {
        log.debug("Lay Ban Chay");
        return ghepDanhSach(
            sanPhamDAO.findBanChayGiay(PageRequest.of(0, SO_GIAY + 5)),
            sanPhamDAO.findBanChayFreesize(PageRequest.of(0, SO_FREESIZE + 2))
        );
    }

    private List<SanPhamDTO> ghepDanhSach(List<SanPham> giay, List<SanPham> freesize) {
        List<SanPham> result = new ArrayList<>(TONG);
        int soFs = Math.min(freesize.size(), SO_FREESIZE);
        result.addAll(freesize.subList(0, soFs));
        int soGiayLay = Math.min(giay.size(), TONG - soFs);
        result.addAll(giay.subList(0, soGiayLay));
        if (result.size() < TONG && giay.size() > soGiayLay) {
            int them = Math.min(giay.size() - soGiayLay, TONG - result.size());
            result.addAll(giay.subList(soGiayLay, soGiayLay + them));
        }
        return result.stream().map(this::chuyenSangDTO).toList();
    }

    private SanPhamDTO chuyenSangDTO(SanPham sp) {
        Integer maSP = sp.getMaSP();
        Double giaGoc = sanPhamChiTietDAO.findGiaThapNhat(maSP).orElse(0.0);
        int km = sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0;
        Double giaSauKM = giaGoc * (100 - km) / 100.0;
        List<String> danhSachAnh = sanPhamChiTietDAO.findDanhSachHinhAnh(maSP);
        String hinhAnh = danhSachAnh.isEmpty() ? null : danhSachAnh.get(0);
        Integer tongSoLuong = sanPhamChiTietDAO.tinhTongSoLuong(maSP);
        return SanPhamDTO.builder()
                .maSP(maSP).tenSP(sp.getTenSP()).hinhAnh(hinhAnh)
                .giaGoc(giaGoc).giaSauKM(giaSauKM).khuyenMai(km)
                .tongSoLuong(tongSoLuong != null ? tongSoLuong : 0)
                .daBan(sp.getDaBan() != null ? sp.getDaBan() : 0)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    //  QUẢN LÝ SẢN PHẨM (Admin)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public SanPham createProduct(SanPhamNDTO dto) {
        SanPham sp = new SanPham();
        sp.setTenSP(dto.getTenSP());
        sp.setMoTa(dto.getMoTa());
        sp.setGioiTinh(dto.getGioiTinh());
        sp.setKhuyenMai(dto.getKhuyenMai() != null ? dto.getKhuyenMai() : 0);
        sp.setDaBan(0);
        sp.setIsActive(true);
        SanPham savedSP = sanPhamDAO.save(sp);

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

        if (dto.getVariants() != null) {
            for (SanPhamNDTO.VariantDTO vDto : dto.getVariants()) {
                SanPhamChiTiet ct = new SanPhamChiTiet();
                ct.setSanPham(savedSP);
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
        return savedSP;
    }

    public SanPhamNDTO getProductDetail(Integer maSP) {
        SanPham sp = sanPhamDAO.findById(maSP).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        SanPhamNDTO dto = new SanPhamNDTO();
        dto.setMaSP(sp.getMaSP());
        dto.setTenSP(sp.getTenSP());
        dto.setMoTa(sp.getMoTa());
        dto.setGioiTinh(sp.getGioiTinh());
        dto.setKhuyenMai(sp.getKhuyenMai());

        if (sp.getSanPhamDanhMucs() != null) {
            List<Integer> catIds = sp.getSanPhamDanhMucs().stream()
                    .map(sdm -> sdm.getDanhMuc().getMaDM())
                    .collect(Collectors.toList());
            dto.setCategoryIds(catIds);
        }

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

    @Transactional
    public SanPham updateProduct(SanPhamNDTO dto) {
        SanPham sp = sanPhamDAO.findById(dto.getMaSP()).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        sp.setTenSP(dto.getTenSP());
        sp.setMoTa(dto.getMoTa());
        sp.setGioiTinh(dto.getGioiTinh());
        sp.setKhuyenMai(dto.getKhuyenMai() != null ? dto.getKhuyenMai() : 0);
        sanPhamDAO.save(sp);

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

        if (dto.getVariants() != null) {
            for (SanPhamNDTO.VariantDTO vDto : dto.getVariants()) {
                SanPhamChiTiet ct = vDto.getMaSKU() != null
                    ? sanPhamChiTietDAO.findById(vDto.getMaSKU()).orElse(new SanPhamChiTiet())
                    : new SanPhamChiTiet();
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
            dto.setKhuyenMai(sp.getKhuyenMai() != null ? sp.getKhuyenMai() : 0);

            if (sp.getSanPhamDanhMucs() != null) {
                String dmStr = sp.getSanPhamDanhMucs().stream()
                        .map(sdm -> sdm.getDanhMuc().getTenDM())
                        .collect(Collectors.joining(", "));
                dto.setDanhMucs(dmStr);
            }

            List<SanPhamChiTiet> chiTiets = sanPhamChiTietDAO.findBySanPham_MaSP(sp.getMaSP());
            dto.setSoPhanLoai(chiTiets.size());
            int tongTon = 0;
            Double giaMin = null, giaMax = null;
            String hinhAnh = null;
            if (!chiTiets.isEmpty()) {
                hinhAnh = chiTiets.get(0).getHinhAnh();
                for (SanPhamChiTiet ct : chiTiets) {
                    tongTon += (ct.getSoLuong() != null ? ct.getSoLuong() : 0);
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
            dto.setGiaDaiDien(giaMin != null ? giaMin : 0.0);
            dto.setHinhAnhDaiDien(hinhAnh);
            if (tongTon == 0) dto.setTrangThai("Hết hàng");
            else if (tongTon <= 10) dto.setTrangThai("Sắp hết");
            else dto.setTrangThai("Còn hàng");
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public boolean toggleProductStatus(Integer maSP) {
        SanPham sp = sanPhamDAO.findById(maSP).orElseThrow(() -> new RuntimeException("Không tìm thấy SP"));
        boolean currentStatus = sp.getIsActive() != null ? sp.getIsActive() : true;
        sp.setIsActive(!currentStatus);
        sanPhamDAO.save(sp);
        return sp.getIsActive();
    }
}
