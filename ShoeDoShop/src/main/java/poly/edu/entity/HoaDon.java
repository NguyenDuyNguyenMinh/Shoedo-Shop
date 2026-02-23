package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "HoaDon")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHD")
    private Integer maHD;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDons"})
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaQT")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hoaDons"})
    private QuanTri quanTri;

    @Column(name = "PhuongThucTT")
    private String phuongThucTT;

    @Column(name = "DiaChiJson", columnDefinition = "nvarchar(max)")
    private String diaChiJson;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "GhiChu", columnDefinition = "nvarchar(max)")
    private String ghiChu;

    @Column(name = "NgayMua")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayMua;

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonCT> hoaDonCTs;
}