package poly.edu.dto.statistics;

import lombok.Data;

@Data
public class OrderStatusCountDTO {
    private String trangThai;
    private Integer soLuong;

    public OrderStatusCountDTO(String trangThai, Integer soLuong) {
        this.trangThai = trangThai;
        this.soLuong = soLuong;
    }
}
