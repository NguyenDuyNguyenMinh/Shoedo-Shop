package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDG")
    private Integer maDG;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHDCT", nullable = false)
    private HoaDonCT hoaDonCT;

    @Column(name = "Sao")
    private Integer sao;

    @Column(name = "DanhGiaCT", columnDefinition = "NVARCHAR(MAX)")
    private String danhGiaCT;

    @Column(name = "NgayDG")
    private LocalDateTime ngayDG;
}