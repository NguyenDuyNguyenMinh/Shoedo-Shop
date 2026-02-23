package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "SanPham_MauSac")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPhamMauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSP_Mau")
    private Integer maSPMau;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSP")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sanPhamMauSacs"})
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaMau")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sanPhamMauSacs"})
    private MauSac mauSac;

    @Column(name = "HinhAnh", columnDefinition = "nvarchar(max)")
    private String hinhAnh;

    @OneToMany(mappedBy = "sanPhamMauSac", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamChiTiet> sanPhamChiTiets;
}