package poly.edu.dto.statistics;

import lombok.Data;
import java.util.List;

@Data
public class StatisticsResponseDTO {
    private List<MonthlyRevenueDTO> doanhThuTheoThang;
    private List<OrderStatusCountDTO> donHangTheoTrangThai;
    private List<TopProductDTO> topSanPham;

    public StatisticsResponseDTO(List<MonthlyRevenueDTO> doanhThuTheoThang,
                                 List<OrderStatusCountDTO> donHangTheoTrangThai,
                                 List<TopProductDTO> topSanPham) {
        this.doanhThuTheoThang = doanhThuTheoThang;
        this.donHangTheoTrangThai = donHangTheoTrangThai;
        this.topSanPham = topSanPham;
    }
}
