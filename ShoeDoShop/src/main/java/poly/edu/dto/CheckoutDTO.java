package poly.edu.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutDTO {
    private Integer maDC;
    private String phuongThucTT;
    private String ghiChu;
    private List<Integer> cartItemIds; // danh sách MaGH được chọn để checkout
}
