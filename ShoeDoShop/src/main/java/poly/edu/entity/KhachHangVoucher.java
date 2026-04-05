package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "KhachHang_Voucher")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KhachHangVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKH_VC")
    private Integer maKHVC;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "khachHangVouchers"})
    private KhachHang khachHang;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaVoucher", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "khachHangVouchers"})
    private Voucher voucher;
    
    @Column(name = "TrangThai")
    private String trangThai = "Chưa sử dụng";
    
    @Column(name = "NgayDoi")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayDoi;
    
    @Column(name = "HanSuDung")
    @Temporal(TemporalType.TIMESTAMP)
    private Date hanSuDung;
    
    @OneToMany(mappedBy = "khachHangVoucher", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<HoaDon> hoaDons;
}