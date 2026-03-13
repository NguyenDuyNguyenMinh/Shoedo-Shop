package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SanPham_ChiTiet")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSKU")
    private Integer maSKU;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSP", nullable = false)
    @JsonIgnore
    private SanPham sanPham;

    @Column(name = "TenMau", length = 50)
    private String tenMau;

    @Column(name = "HinhAnh", columnDefinition = "NVARCHAR(MAX)")
    private String hinhAnh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSize")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Size size;

    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Column(name = "SoLuong")
    private Integer soLuong = 0;

    @Column(name = "DonGia")
    private Double donGia;
}