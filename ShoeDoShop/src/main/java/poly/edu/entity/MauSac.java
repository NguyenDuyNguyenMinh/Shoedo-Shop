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

    @Column(name = "TenMau", unique = true)
    private String tenMau;

    @Column(name = "MaHex")
    private String maHex;

    @OneToMany(mappedBy = "mauSac", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamMauSac> sanPhamMauSacs;
}