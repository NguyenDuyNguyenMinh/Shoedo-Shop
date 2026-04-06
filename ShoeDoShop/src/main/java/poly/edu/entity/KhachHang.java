package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "khachhang")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKH")
    private Integer maKH;

    @Column(name = "TenKH", nullable = false, length = 100)
    private String tenKH;

    @Column(name = "SDT")
    private String sdt;
    
    @Column(name = "DiemTichLuy")
    private Integer diemTichLuy = 0;
    
    @Column(name = "MaGioiThieu", unique = true, nullable = false, length = 20)
    private String maGioiThieu;
    
    @Column(name = "MaNguoiGioiThieu", length = 20)
    private String maNguoiGioiThieu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaUser")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "khachHang", "quanTri"})
    private Users user;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DiaChi> diaChis;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GioHang> gioHangs;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDon> hoaDons;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TimKiem> timKiems;
    
    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<KhachHangVoucher> khachHangVouchers;
    
    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<LichSuTichDiem> lichSuTichDiems;
}