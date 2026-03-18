package poly.edu.dto;

import lombok.Data;
import java.util.List;

@Data
public class SanPhamNDTO {
    // ĐÃ CHUYỂN BIẾN NÀY LÊN ĐÚNG VỊ TRÍ CỦA SẢN PHẨM
    private Integer maSP; 
    
    // Thông tin Sản phẩm
    private String tenSP;
    private String moTa;
    private Boolean gioiTinh; // 1: Nam, 0: Nữ, null: Unisex
    private Integer khuyenMai;

    // Danh sách ID danh mục được chọn
    private List<Integer> categoryIds;

    // Danh sách các biến thể (Chi tiết sản phẩm)
    private List<VariantDTO> variants;

    @Data
    public static class VariantDTO {
        // Biến này giữ nguyên cho phân loại
        private Integer maSKU; 
        
        private String tenMau;
        private Integer maSize;
        private Double donGia;
        private Integer soLuong;
        private String hinhAnh;
        private String trangThai;
    }
}