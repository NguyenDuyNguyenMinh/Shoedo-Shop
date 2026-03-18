package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "DanhGia")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDG")
    private Integer maDG;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaHDCT", unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "danhGia"})
    private HoaDonCT hoaDonCT;

    @Column(name = "Sao")
    private Integer sao;

    @Column(name = "DanhGiaCT", columnDefinition = "nvarchar(max)")
    private String danhGiaCT;

    @Column(name = "NgayDG")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayDG;
}