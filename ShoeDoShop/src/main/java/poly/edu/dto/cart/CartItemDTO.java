package poly.edu.dto.cart;

import lombok.Data;

@Data
public class CartItemDTO {
    private Integer maGH;
    private Integer maSKU;
    private String tenSP;
    private String hinhAnh;
    private String tenMau;
    private Integer coGiay;
    private Integer soLuong;
    private Double donGia;
    private Double thanhTien;
}
