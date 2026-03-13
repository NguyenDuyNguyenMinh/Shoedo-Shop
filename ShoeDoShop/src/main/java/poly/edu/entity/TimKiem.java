package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "TimKiem")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TimKiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTK")
    private Integer maTK;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "timKiems"})
    private KhachHang khachHang;

    @Column(name = "NoiDungTimKiem", nullable = false)
    private String noiDungTimKiem;

    @Column(name = "ThoiGian")
    private LocalDateTime thoiGian;
}