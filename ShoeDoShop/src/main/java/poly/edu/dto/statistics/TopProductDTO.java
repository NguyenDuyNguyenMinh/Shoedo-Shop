package poly.edu.dto.statistics;

import lombok.Data;

@Data
public class TopProductDTO {
    private Integer maSKU;
    private String tenSP;
    private Integer tongSoLuongBan;

    public TopProductDTO(Integer maSKU, String tenSP, Integer tongSoLuongBan) {
        this.maSKU = maSKU;
        this.tenSP = tenSP;
        this.tongSoLuongBan = tongSoLuongBan;
    }
}
