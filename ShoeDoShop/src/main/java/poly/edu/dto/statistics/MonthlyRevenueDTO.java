package poly.edu.dto.statistics;

import lombok.Data;

@Data
public class MonthlyRevenueDTO {
    private Integer nam;
    private Integer thang;
    private Double doanhThu;
    private Integer soDonHang;

    public MonthlyRevenueDTO(Integer nam, Integer thang, Double doanhThu, Integer soDonHang) {
        this.nam = nam;
        this.thang = thang;
        this.doanhThu = doanhThu;
        this.soDonHang = soDonHang;
    }
}
