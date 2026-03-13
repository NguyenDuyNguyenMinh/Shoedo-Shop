package poly.edu.dto;

import lombok.*;


/**
 * DTO gọn cho sản phẩm liên quan hiển thị ở cuối trang DetailProduct.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamLienQuanDTO {

    private Integer    maSP;
    private String     tenSP;
    private String     hinhAnh;
    private Double giaGoc;
    private Double giaSauKM;
    private Integer    khuyenMai;
    private Integer    tongSoLuong;
    private Integer    daBan;
    private String     tenDanhMuc;
}