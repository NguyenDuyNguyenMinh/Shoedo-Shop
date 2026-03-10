package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

/**
 * Entity ánh xạ bảng trung gian SanPham_DanhMuc.
 * Composite PK (MaSP, MaDM) — dùng @IdClass với inner class SanPhamDanhMucId.
 */
@Entity
@Data
@Table(name = "SanPham_DanhMuc")
@IdClass(SanPhamDanhMuc.SanPhamDanhMucId.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPhamDanhMuc {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSP")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SanPham sanPham;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDM")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DanhMuc danhMuc;

    // ── Composite PK class (inner static) ──────────────────────────
    @Data
    public static class SanPhamDanhMucId implements Serializable {
        private Integer sanPham; // phải khớp tên field @Id bên trên
        private Integer danhMuc; // phải khớp tên field @Id bên trên
    }
}