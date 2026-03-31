package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "LichSuTichDiem")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LichSuTichDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLS")
    private Integer maLS;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lichSuTichDiems"})
    private KhachHang khachHang;
    
    @Column(name = "SoDiem")
    private Integer soDiem;
    
    @Column(name = "LoaiGiaoDich", length = 100)
    private String loaiGiaoDich;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaNguoiLienQuan")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private KhachHang nguoiLienQuan;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaHDCT")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lichSuTichDiem"})
    private HoaDonCT hoaDonCT;
    
    @Column(name = "NgayGiaoDich")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayGiaoDich;
}