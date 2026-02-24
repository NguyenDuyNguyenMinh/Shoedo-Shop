package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "MauSac")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaMau")
    private Integer maMau;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSP")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "mauSacs"})
    private SanPham sanPham;

    @Column(name = "TenMau")
    private String tenMau;
    
    @Column(name = "HinhAnh", columnDefinition = "nvarchar(max)")
    private String hinhAnh;

    @OneToMany(mappedBy = "mauSac", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamChiTiet> sanPhamChiTiets;
}