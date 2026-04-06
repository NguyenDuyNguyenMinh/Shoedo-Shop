package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "phieunhap")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NhapKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNK")
    private Integer maNK;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSKU")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "phieuNhaps"})
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "NgayNhap")
    @Temporal(TemporalType.DATE)
    private Date ngayNhap;
}