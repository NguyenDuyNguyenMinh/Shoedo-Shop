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

    @ManyToOne
    // Thêm insertable = false, updatable = false vào đây
    @JoinColumn(name = "MaSP", insertable = false, updatable = false) 
    @JsonIgnoreProperties("sanPhamDanhMucs")
    private SanPham sanPham;

    @ManyToOne
    // Thêm insertable = false, updatable = false vào đây
    @JoinColumn(name = "MaDM", insertable = false, updatable = false) 
    @JsonIgnoreProperties("sanPhamDanhMucs") 
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