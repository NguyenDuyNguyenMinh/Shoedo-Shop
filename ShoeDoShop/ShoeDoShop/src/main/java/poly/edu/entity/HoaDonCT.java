package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "HoaDonCT")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HoaDonCT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHDCT")
    private Integer maHDCT;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaHD")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDonCTs"})
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSKU")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDonCTs"})
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia")
    private Double donGia;

    @OneToOne(mappedBy = "hoaDonCT", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("hoaDonCT")
    private DanhGia danhGia;
}