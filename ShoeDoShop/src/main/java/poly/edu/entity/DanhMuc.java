package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "DanhMuc")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDM")
    private Integer maDM;

    @Column(name = "TenDM", nullable = false)
    private String tenDM;

    @OneToMany(mappedBy = "danhMuc", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamDanhMuc> sanPhamDanhMucs;
}