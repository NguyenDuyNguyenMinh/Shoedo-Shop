package poly.edu.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO trả dữ liệu sản phẩm ra trang Index (Flash Sales, Nổi Bật, Bán Chạy).
 *
 * Giá hiển thị = DonGia thấp nhất trong các SKU còn hàng,
 * đã áp dụng % KhuyenMai.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamDTO {

    /** Mã sản phẩm */
    private Integer maSP;

    /** Tên sản phẩm */
    private String tenSP;

    /** URL / tên file ảnh đại diện (SKU đầu tiên còn hàng) */
    private String hinhAnh;

    /** Giá gốc thấp nhất (chưa khuyến mãi) */
    private BigDecimal giaGoc;

    /** Giá sau khuyến mãi = giaGoc * (100 - khuyenMai) / 100 */
    private BigDecimal giaSauKM;

    /** % khuyến mãi (0 – 100) */
    private Integer khuyenMai;

    /** Tổng số lượng tồn kho (cộng tất cả SKU) */
    private Integer tongSoLuong;

    /** Số lần đã bán */
    private Integer daBan;
}