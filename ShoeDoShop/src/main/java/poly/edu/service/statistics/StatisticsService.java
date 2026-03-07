package poly.edu.service.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.HoaDonCTDAO;
import poly.edu.dao.HoaDonDAO;
import poly.edu.dto.statistics.MonthlyRevenueDTO;
import poly.edu.dto.statistics.OrderStatusCountDTO;
import poly.edu.dto.statistics.StatisticsResponseDTO;
import poly.edu.dto.statistics.TopProductDTO;
import poly.edu.entity.HoaDon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private HoaDonDAO hoaDonDAO;

    @Autowired
    private HoaDonCTDAO hoaDonCTDAO;

    /**
     * Lấy tất cả thống kê
     * @param nam Năm cần thống kê (null = tất cả năm)
     * @return StatisticsResponseDTO
     */
    public StatisticsResponseDTO getAllStatistics(Integer nam) {
        List<MonthlyRevenueDTO> doanhThuTheoThang = getMonthlyRevenue(nam);
        List<OrderStatusCountDTO> donHangTheoTrangThai = getOrderStatusCount();
        List<TopProductDTO> topSanPham = getTopProducts(5);

        return new StatisticsResponseDTO(
                doanhThuTheoThang,
                donHangTheoTrangThai,
                topSanPham
        );
    }

    /**
     * Thống kê doanh thu theo tháng
     * Chỉ tính các đơn hàng có TrangThai = N'Hoàn tất'
     * @param nam Năm cần thống kê (null = tất cả năm)
     * @return Danh sách MonthlyRevenueDTO
     */
    public List<MonthlyRevenueDTO> getMonthlyRevenue(Integer nam) {
        List<HoaDon> hoaDons;

        if (nam != null) {
            // Lọc theo năm
            hoaDons = hoaDonDAO.findAll().stream()
                    .filter(hd -> hd.getNgayMua() != null
                            && hd.getNgayMua().getYear() + 1900 == nam
                            && "Hoàn tất".equals(hd.getTrangThai()))
                    .collect(Collectors.toList());
        } else {
            // Tất cả năm
            hoaDons = hoaDonDAO.findAll().stream()
                    .filter(hd -> hd.getNgayMua() != null
                            && "Hoàn tất".equals(hd.getTrangThai()))
                    .collect(Collectors.toList());
        }

        // Gom nhóm theo tháng và năm
        java.util.Map<String, MonthlyRevenueDTO> grouped = new java.util.LinkedHashMap<>();

        for (HoaDon hd : hoaDons) {
            int year = hd.getNgayMua().getYear() + 1900;
            int month = hd.getNgayMua().getMonth() + 1;
            String key = year + "-" + month;

            // Tính tổng tiền từ HoaDonCT
            double tongTien = 0;
            if (hd.getHoaDonCTs() != null) {
                for (var hdct : hd.getHoaDonCTs()) {
                    tongTien += hdct.getDonGia() * hdct.getSoLuong();
                }
            }

            if (grouped.containsKey(key)) {
                MonthlyRevenueDTO existing = grouped.get(key);
                existing.setDoanhThu(existing.getDoanhThu() + tongTien);
                existing.setSoDonHang(existing.getSoDonHang() + 1);
            } else {
                grouped.put(key, new MonthlyRevenueDTO(year, month, tongTien, 1));
            }
        }

        return new ArrayList<>(grouped.values());
    }

    /**
     * Đếm số lượng đơn hàng theo từng trạng thái
     * @return Danh sách OrderStatusCountDTO
     */
    public List<OrderStatusCountDTO> getOrderStatusCount() {
        List<HoaDon> hoaDons = hoaDonDAO.findAll();

        java.util.Map<String, Integer> grouped = new java.util.LinkedHashMap<>();

        for (HoaDon hd : hoaDons) {
            String trangThai = hd.getTrangThai();
            if (trangThai == null) {
                trangThai = "Không xác định";
            }

            grouped.put(trangThai, grouped.getOrDefault(trangThai, 0) + 1);
        }

        List<OrderStatusCountDTO> result = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : grouped.entrySet()) {
            result.add(new OrderStatusCountDTO(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    /**
     * Lấy top sản phẩm bán chạy nhất
     * @param limit Số lượng sản phẩm cần lấy (mặc định 5)
     * @return Danh sách TopProductDTO
     */
    public List<TopProductDTO> getTopProducts(Integer limit) {
        List<poly.edu.entity.HoaDonCT> allDetails = hoaDonCTDAO.findAll();

        // Gom nhóm theo MaSKU và tính tổng số lượng bán
        java.util.Map<Integer, Integer> productSales = new java.util.LinkedHashMap<>();

        for (poly.edu.entity.HoaDonCT hdct : allDetails) {
            Integer maSKU = hdct.getSanPhamChiTiet().getMaSKU();
            Integer soLuong = hdct.getSoLuong();

            productSales.put(maSKU, productSales.getOrDefault(maSKU, 0) + soLuong);
        }

        // Sắp xếp giảm dần theo số lượng bán
        List<java.util.Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(productSales.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Lấy top N
        List<TopProductDTO> result = new ArrayList<>();
        int count = 0;
        for (java.util.Map.Entry<Integer, Integer> entry : sortedEntries) {
            if (count >= limit) break;

            // Lấy tên sản phẩm
            Integer maSKU = entry.getKey();
            String tenSP = "Sản phẩm " + maSKU;

            // Try to get product name from HoaDonCT -> SanPhamChiTiet -> MauSac -> SanPham
            for (poly.edu.entity.HoaDonCT hdct : allDetails) {
                if (hdct.getSanPhamChiTiet().getMaSKU().equals(maSKU)
                        && hdct.getSanPhamChiTiet().getMauSac() != null
                        && hdct.getSanPhamChiTiet().getMauSac().getSanPham() != null) {
                    tenSP = hdct.getSanPhamChiTiet().getMauSac().getSanPham().getTenSP();
                    break;
                }
            }

            result.add(new TopProductDTO(maSKU, tenSP, entry.getValue()));
            count++;
        }

        return result;
    }
}
