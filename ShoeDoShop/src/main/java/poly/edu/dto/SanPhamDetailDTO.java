package poly.edu.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanPhamDetailDTO {
    private Integer         maSP;
    private String          tenSP;
    private String          moTa;
    private Boolean         gioiTinh;
    private Integer         khuyenMai;
    private Integer         daBan;
    private List<String>    danhMucs;
    private Double          giaGoc;
    private Double          giaSauKM;
    private Integer         tongSoLuong;
    private List<String>    danhSachMau;
    private List<Integer>   danhSachSize;
    private List<String>    danhSachHinhAnh;
    private List<SanPhamChiTietDTO> chiTiets;
}