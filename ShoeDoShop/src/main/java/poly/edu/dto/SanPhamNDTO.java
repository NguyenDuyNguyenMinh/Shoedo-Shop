package poly.edu.dto;

import lombok.Data;
import java.util.List;

@Data
public class SanPhamNDTO {
    private Integer maSP; 
    private String tenSP;
    private String moTa;
    private Boolean gioiTinh;
    private Integer khuyenMai;
    private List<Integer> categoryIds;
    private List<VariantDTO> variants;

    @Data
    public static class VariantDTO {
        private Integer maSKU; 
        private String tenMau;
        private Integer maSize;
        private Double donGia;
        private Integer soLuong;
        private String hinhAnh;
        private String trangThai;
    }
}