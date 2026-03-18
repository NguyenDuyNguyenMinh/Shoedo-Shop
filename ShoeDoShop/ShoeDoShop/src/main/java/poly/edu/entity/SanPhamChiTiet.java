package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "SanPham_ChiTiet")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSKU")
    private Integer maSKU;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSP")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sanPhamChiTiets"})
    private SanPham sanPham;

    @Column(name = "TenMau")
    private String tenMau;
    
    @Column(name = "HinhAnh", columnDefinition = "nvarchar(max)")
    private String hinhAnh;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSize")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sanPhamChiTiets"})
    private Size size;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia")
    private Double donGia;

    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<NhapKho> nhapKhos;

    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GioHang> gioHangs;

    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonCT> hoaDonCTs;
}