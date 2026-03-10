package poly.edu.dto;

import lombok.*;
import java.math.BigDecimal;

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
    private BigDecimal giaGoc;
    private BigDecimal giaSauKM;
    private Integer    khuyenMai;
    private Integer    tongSoLuong;
    private Integer    daBan;
    private String     tenDanhMuc;
}