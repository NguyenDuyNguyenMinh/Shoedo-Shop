package poly.edu.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutDTO {
    private Integer maDC;
    private String phuongThucTT;
    private String ghiChu;
    private List<Integer> cartItemIds;
    private Boolean isVNPay;
    /** ID của bản ghi KhachHang_Voucher mà khách hàng chọn áp dụng */
    private Integer maKH_VC;
    private String refCode;
}
