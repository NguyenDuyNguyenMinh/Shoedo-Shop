package poly.edu.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO cho từng SKU (màu + size + tồn kho + giá).
 * Dùng trong SanPhamDetailDTO.chiTiets để Vue biết
 * tổ hợp nào còn hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamChiTietDTO {

    private Integer maSKU;
    private String  tenMau;
    private String  hinhAnh;
    private Integer coGiay;      // CoGiay từ bảng Size (0 = freesize)
    private Integer soLuong;
    private BigDecimal donGia;
    private String  trangThai;
}