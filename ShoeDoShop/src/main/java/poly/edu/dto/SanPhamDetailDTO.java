package poly.edu.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO đầy đủ cho trang DetailProduct.
 * Gồm: thông tin sản phẩm, danh sách màu, size, ảnh, từng SKU.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamDetailDTO {

    private Integer         maSP;
    private String          tenSP;
    private String          moTa;
    private Boolean         gioiTinh;    // true=Nam, false=Nữ, null=Unisex
    private Integer         khuyenMai;   // 0–100
    private Integer         daBan;

    /** Danh sách tên danh mục */
    private List<String>    danhMucs;

    /** Giá gốc thấp nhất (SKU còn hàng) */
    private BigDecimal      giaGoc;

    /** Giá sau khuyến mãi */
    private BigDecimal      giaSauKM;

    /** Tổng tồn kho */
    private Integer         tongSoLuong;

    /** Danh sách màu unique (giữ thứ tự) */
    private List<String>    danhSachMau;

    /** Danh sách size unique, sort tăng dần */
    private List<Integer>   danhSachSize;

    /** Danh sách ảnh unique (1 ảnh / màu) */
    private List<String>    danhSachHinhAnh;

    /** Chi tiết từng SKU — Vue dùng để biết tổ hợp nào còn hàng */
    private List<SanPhamChiTietDTO> chiTiets;
}