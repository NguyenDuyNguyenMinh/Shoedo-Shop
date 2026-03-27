package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime thoiGianBatDau;
    
    @Column(name = "ThoiGianKetThuc", nullable = false)
    private LocalDateTime thoiGianKetThuc;
    
    @Column(name = "TrangThai", length = 50)
    private String trangThai = "Đang chạy";
    
    @Transient
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "Đang chạy".equals(trangThai) 
                && now.isAfter(thoiGianBatDau) 
                && now.isBefore(thoiGianKetThuc);
    }
    
    public void updateTrangThai() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(thoiGianKetThuc)) {
            this.trangThai = "Kết thúc";
        } else if (now.isBefore(thoiGianBatDau)) {
            this.trangThai = "Đang chạy";
        }
    }
}