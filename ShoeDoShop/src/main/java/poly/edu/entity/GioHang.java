package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "GioHang")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGH")
    private Integer maGH;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "gioHangs"})
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSKU")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "gioHangs"})
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "SoLuong")
    private Integer soLuong;

    /**
     * Optimistic locking — ngăn race condition khi nhiều request cùng cập nhật giỏ hàng.
     * Khi 2 thread đồng thời update, thread thứ 2 sẽ throw OptimisticLockException.
     */
    @Version
    private Integer version;
}