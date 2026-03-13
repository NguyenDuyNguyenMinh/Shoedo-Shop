package poly.edu.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanPhamListDTO {
    private Integer      maSP;
    private String       tenSP;
    private Boolean      gioiTinh;
    private String       moTa;
    private Integer      khuyenMai;
    private Double       giaGoc;
    private Double       giaSauKM;
    private String       hinhAnh;
    private Integer      tongSoLuong;
    private List<String> danhMucs;
    private Boolean      conHang;
    private Integer      daBan;
}