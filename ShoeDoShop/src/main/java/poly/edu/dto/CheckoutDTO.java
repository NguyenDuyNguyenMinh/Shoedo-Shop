package poly.edu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutDTO {

    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    private Integer maDC;

    @NotBlank(message = "Phương thức thanh toán không hợp lệ")
    private String phuongThucTT;

    private String ghiChu;

    @Min(value = 1, message = "Phải chọn ít nhất 1 sản phẩm")
    private List<Integer> cartItemIds;

    private Boolean isVNPay;

    /** UUID từ frontend — dùng để chống double-submit (idempotency key) */
    private String idempotencyKey;

    /**
     * Loại thanh toán VNPay: "REDIRECT" (redirect sang cổng VNPay).
     */
    private String paymentType;
}
