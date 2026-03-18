package poly.edu.dto;

import lombok.Data;
import java.util.Date;

@Data
public class HoaDonDTO {
    private Integer maHD;
    private Date ngayMua;
    private String phuongThucTT;
    private String trangThai;
    private String ghiChu;
}
