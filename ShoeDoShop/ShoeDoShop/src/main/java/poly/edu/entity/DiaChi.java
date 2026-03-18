package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "DiaChi")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDC")
    private Integer maDC;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKH")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "diaChis"})
    private KhachHang khachHang;

    @Column(name = "MacDinh")
    private Boolean macDinh;

    @Column(name = "DiemGiao")
    private String diemGiao;

    @Column(name = "TenNN")
    private String tenNN;

    @Column(name = "SDT")
    private String sdt;
}