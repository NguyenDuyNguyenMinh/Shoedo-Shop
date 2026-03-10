package poly.edu.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO trả về cho trang KH_SanPham (danh sách sản phẩm).
 * Mỗi object đại diện 1 sản phẩm với giá thấp nhất và ảnh đại diện.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamListDTO {

    private Integer    maSP;
    private String     tenSP;

    /** null = Unisex/Phụ kiện | true = Nam | false = Nữ */
    private Boolean    gioiTinh;

    private String     moTa;

    /** Phần trăm khuyến mãi (0–100) */
    private Integer    khuyenMai;

    /** Giá gốc thấp nhất (trước KM) — chỉ gửi khi khuyenMai > 0 */
    private BigDecimal giaGoc;

    /** Giá sau khuyến mãi = giaGoc * (100 - khuyenMai) / 100 */
    private BigDecimal giaSauKM;

    /** Ảnh đại diện (SKU đầu tiên còn hàng) */
    private String     hinhAnh;

    /** Tổng tồn kho tất cả SKU */
    private Integer    tongSoLuong;

    /** Danh sách tên danh mục */
    private List<String> danhMucs;

    /** Còn hàng hay không */
    private Boolean    conHang;

    /** Số lần đã bán (dùng cho sort Bán Chạy) */
    private Integer    daBan;
}