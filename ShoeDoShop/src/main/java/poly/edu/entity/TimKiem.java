package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TimKiem")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TimKiem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTK")
    private Integer maTK;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKH", nullable = false)
    private KhachHang khachHang;

    @Column(name = "NoiDungTimKiem", nullable = false, length = 225)
    private String noiDungTimKiem;

    @Column(name = "ThoiGian")
    private LocalDateTime thoiGian;

    @PrePersist
    public void prePersist() {
        if (thoiGian == null) thoiGian = LocalDateTime.now();
    }
}