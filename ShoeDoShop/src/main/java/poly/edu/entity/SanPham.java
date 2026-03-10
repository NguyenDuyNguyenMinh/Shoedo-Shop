package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "SanPham")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSP")
    private Integer maSP;

    @Column(name = "TenSP", nullable = false, length = 200)
    private String tenSP;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "KhuyenMai")
    private Integer khuyenMai = 0;

    @Column(name = "DaBan")
    private Integer daBan = 0;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamChiTiet> chiTietList;

    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamDanhMuc> sanPhamDanhMucs;
}