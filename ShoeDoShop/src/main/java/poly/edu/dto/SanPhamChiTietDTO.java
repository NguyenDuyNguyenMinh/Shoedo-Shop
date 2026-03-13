package poly.edu.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanPhamChiTietDTO {
    private Integer maSKU;
    private String  tenMau;
    private String  hinhAnh;
    private Integer coGiay;
    private Integer soLuong;
    private Double  donGia;
    private String  trangThai;
}