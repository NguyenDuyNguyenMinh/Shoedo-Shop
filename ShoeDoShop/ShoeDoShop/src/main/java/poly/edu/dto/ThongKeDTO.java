package poly.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeDTO {
    private Date thoiGian;
    private Double doanhThu;
    private Integer soDonHang;
    private Double giaTriDonTrungBinh;
    private Integer soSanPhamBanRa;
}
