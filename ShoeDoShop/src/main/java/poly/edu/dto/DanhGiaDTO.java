package poly.edu.dto;

import lombok.*;
import java.util.Date;

/**
 * DTO trả về cho trang DetailProduct — tab Đánh giá.
 * Join: DanhGia → HoaDonCT → HoaDon → KhachHang để lấy TenKH.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DanhGiaDTO {

    private Integer maDG;
    private Integer sao;
    private String  danhGiaCT;   // nội dung đánh giá
    private Date    ngayDG;

    // Thông tin người đánh giá (lấy từ KhachHang)
    private String tenKH;        // tên khách hàng
}
