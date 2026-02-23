package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "QuanTri")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QuanTri {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaQT")
    private Integer maQT;

    @Column(name = "TenQT")
    private String tenQT;

    @Column(name = "Role")
    private Boolean role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaUser")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "khachHang", "quanTri"})
    private Users user;

    @OneToMany(mappedBy = "quanTri", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDon> hoaDons;
}