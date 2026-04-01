package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "Voucher")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVoucher")
    private Integer maVoucher;
    
    @Column(name = "TenVoucher", nullable = false, length = 100)
    private String tenVoucher;
    
    @Column(name = "DiemCanDoi", nullable = false)
    private Integer diemCanDoi;
    
    @Column(name = "GiaTriGiam")
    private Double giaTriGiam;
    
    @Column(name = "DonToiThieu")
    private Double donToiThieu = 0.0;
    
    @Column(name = "SoLuong")
    private Integer soLuong = 0;
    
    @Column(name = "NgayBatDau")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Ho_Chi_Minh")
    private Date ngayBatDau;
    
    @Column(name = "NgayKetThuc")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Ho_Chi_Minh")
    private Date ngayKetThuc;
    
    @Column(name = "IsActive")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<KhachHangVoucher> khachHangVouchers;
}