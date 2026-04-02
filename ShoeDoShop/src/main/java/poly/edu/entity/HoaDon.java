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
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH_VC")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private KhachHangVoucher khachHangVoucher;

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
    
    @Column(name = "NgayDen")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayDen;

    // /** Lưu tạm cartItemIds (dạng JSON) khi checkout VNPay — dùng để restore cart khi hủy thanh toán */
    // @Column(name = "CartItemIdsJson", columnDefinition = "nvarchar(max)")
    // private String cartItemIdsJson;

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonCT> hoaDonCTs;
}
