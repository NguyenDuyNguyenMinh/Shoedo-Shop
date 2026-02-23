package poly.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Entity
@Data
@Table(name = "SanPham_DanhMuc")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SanPhamDanhMuc {
    
    @EmbeddedId
    private SanPhamDanhMucId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSP", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sanPhamDanhMucs"})
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaDM", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sanPhamDanhMucs"})
    private DanhMuc danhMuc;

    @Embeddable
    @Data
    public static class SanPhamDanhMucId implements Serializable {
        @Column(name = "MaSP")
        private Integer maSP;

        @Column(name = "MaDM")
        private Integer maDM;
    }
}