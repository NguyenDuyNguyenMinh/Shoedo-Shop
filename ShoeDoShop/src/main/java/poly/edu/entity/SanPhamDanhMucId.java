package poly.edu.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * Composite Primary Key cho bảng trung gian SanPham_DanhMuc.
 * Tên field phải khớp đúng với tên field @Id trong SanPhamDanhMuc.
 */
@Data
public class SanPhamDanhMucId implements Serializable {
    private Integer sanPham; // khớp với field "sanPham" trong SanPhamDanhMuc
    private Integer danhMuc; // khớp với field "danhMuc" trong SanPhamDanhMuc
}