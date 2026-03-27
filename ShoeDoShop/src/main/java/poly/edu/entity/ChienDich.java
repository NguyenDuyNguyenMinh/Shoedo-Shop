package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "ChienDich")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChienDich {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCD")
    private Integer maCD;
    
    @Column(name = "TenChienDich", nullable = false, length = 255)
    private String tenChienDich;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSP", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "chienDichs"})
    private SanPham sanPham;
    
    @Column(name = "KhuyenMaiCD")
    private Integer khuyenMaiCD;

    @Column(name = "ThoiGianBatDau", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date thoiGianBatDau;
    
    @Column(name = "ThoiGianKetThuc", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date thoiGianKetThuc;
    
    
    @Column(name = "TrangThai", length = 50)
    private String trangThai = "Chưa bắt đầu";
    
    @Transient
    public boolean isActive() {
        Date now = new Date();
        return "Đang chạy".equals(trangThai) 
                && now.after(thoiGianBatDau) 
                && now.before(thoiGianKetThuc);
    }
    
    public void updateTrangThai() {
        Date now = new Date();
        if (now.after(thoiGianKetThuc)) {
            this.trangThai = "Kết thúc";
        } else if (now.before(thoiGianBatDau)) {
            this.trangThai = "Đang chạy";
        }
    }
}