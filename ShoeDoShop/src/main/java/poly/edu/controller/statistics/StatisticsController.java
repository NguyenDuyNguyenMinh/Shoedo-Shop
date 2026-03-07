package poly.edu.controller.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.statistics.MonthlyRevenueDTO;
import poly.edu.dto.statistics.OrderStatusCountDTO;
import poly.edu.dto.statistics.StatisticsResponseDTO;
import poly.edu.dto.statistics.TopProductDTO;
import poly.edu.service.statistics.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * GET /api/statistics
     * Lấy tất cả thống kê (doanh thu, đơn hàng, top sản phẩm)
     * @param nam Năm cần thống kê (optional)
     */
    @GetMapping
    public ResponseEntity<StatisticsResponseDTO> getAllStatistics(
            @RequestParam(required = false) Integer nam) {
        StatisticsResponseDTO statistics = statisticsService.getAllStatistics(nam);
        return ResponseEntity.ok(statistics);
    }

    /**
     * GET /api/statistics/revenue
     * Thống kê doanh thu theo tháng
     * @param nam Năm cần thống kê (optional)
     */
    @GetMapping("/revenue")
    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenue(
            @RequestParam(required = false) Integer nam) {
        List<MonthlyRevenueDTO> revenue = statisticsService.getMonthlyRevenue(nam);
        return ResponseEntity.ok(revenue);
    }

    /**
     * GET /api/statistics/orders/status
     * Đếm số lượng đơn hàng theo trạng thái
     */
    @GetMapping("/orders/status")
    public ResponseEntity<List<OrderStatusCountDTO>> getOrderStatusCount() {
        List<OrderStatusCountDTO> statusCounts = statisticsService.getOrderStatusCount();
        return ResponseEntity.ok(statusCounts);
    }

    /**
     * GET /api/statistics/products/top
     * Lấy top sản phẩm bán chạy
     * @param limit Số lượng sản phẩm (default = 5)
     */
    @GetMapping("/products/top")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(defaultValue = "5") Integer limit) {
        List<TopProductDTO> topProducts = statisticsService.getTopProducts(limit);
        return ResponseEntity.ok(topProducts);
    }
}
