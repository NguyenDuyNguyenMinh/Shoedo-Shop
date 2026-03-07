package poly.edu.dto.checkout;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequestDTO {
    private Integer maKH;
    private Integer maDC;
    private String ghiChu;
    private List<Integer> danhSachMaSKU;
}
