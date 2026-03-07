package poly.edu.dto.checkout;

import lombok.Data;

@Data
public class CheckoutResponseDTO {
    private Integer maHD;
    private String phuongThucTT;
    private String trangThai;
    private String diaChiJson;
    private Double tongTien;
    private String message;

    public CheckoutResponseDTO(Integer maHD, String phuongThucTT, String trangThai,
                                String diaChiJson, Double tongTien, String message) {
        this.maHD = maHD;
        this.phuongThucTT = phuongThucTT;
        this.trangThai = trangThai;
        this.diaChiJson = diaChiJson;
        this.tongTien = tongTien;
        this.message = message;
    }
}
