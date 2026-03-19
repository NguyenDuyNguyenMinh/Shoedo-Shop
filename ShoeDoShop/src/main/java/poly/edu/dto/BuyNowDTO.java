package poly.edu.dto;

import lombok.Data;

@Data
public class BuyNowDTO {
    private Integer maSKU;
    private Integer soLuong;
    private Integer maDC;
    private String phuongThucTT;
    private String ghiChu;
    private Boolean isVNPay;
    private Boolean isQRCode;
}
