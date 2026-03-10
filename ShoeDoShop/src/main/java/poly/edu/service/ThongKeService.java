package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.ThongKeDAO;
import poly.edu.dto.ThongKeDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ThongKeService {

    @Autowired
    private ThongKeDAO thongKeDAO;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

    /**
     * Thống kê doanh thu theo ngày
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với doanh thu, số đơn hàng, giá trị đơn TB, số sản phẩm theo ngày
     */
    public List<ThongKeDTO> thongKeTheoNgay(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoNgay(startDate, endDate);
        return mapResultsToThongKeDTO(results, "day");
    }

    /**
     * Thống kê doanh thu theo tháng
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với doanh thu, số đơn hàng, giá trị đơn TB, số sản phẩm theo tháng
     */
    public List<ThongKeDTO> thongKeTheoThang(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoThang(startDate, endDate);
        return mapResultsToThongKeDTO(results, "month");
    }

    /**
     * Thống kê doanh thu theo năm
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với doanh thu, số đơn hàng, giá trị đơn TB, số sản phẩm theo năm
     */
    public List<ThongKeDTO> thongKeTheoNam(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoNam(startDate, endDate);
        return mapResultsToThongKeDTO(results, "year");
    }

    /**
     * Map kết quả SQL sang DTO
     * 
     * @param results Kết quả từ native query
     * @param type Loại thống kê (day/month/year)
     * @return List<ThongKeDTO>
     */
    private List<ThongKeDTO> mapResultsToThongKeDTO(List<Object[]> results, String type) {
        List<ThongKeDTO> dtos = new ArrayList<>();
        
        for (Object[] row : results) {
            ThongKeDTO dto = new ThongKeDTO();
            
            try {
                if ("day".equals(type)) {
                    dto.setThoiGian(dateFormat.parse(row[0].toString()));
                } else if ("month".equals(type)) {
                    dto.setThoiGian(monthFormat.parse(row[0].toString()));
                } else if ("year".equals(type)) {
                    dto.setThoiGian(dateFormat.parse(row[0].toString() + "-01-01"));
                }
            } catch (ParseException e) {
                dto.setThoiGian(new Date());
            }
            
            dto.setDoanhThu(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
            dto.setSoDonHang(row[2] != null ? ((Number) row[2]).intValue() : 0);
            dto.setGiaTriDonTrungBinh(row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
            dto.setSoSanPhamBanRa(row[4] != null ? ((Number) row[4]).intValue() : 0);
            
            dtos.add(dto);
        }
        
        return dtos;
    }

    /**
     * Thống kê tổng quan (Dashboard)
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Map chứa các chỉ số tổng quan
     */
    public Map<String, Object> thongKeTongQuan(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTongQuan(startDate, endDate);
        Map<String, Object> dashboard = new HashMap<>();
        
        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            dashboard.put("tongDoanhThu", row[0] != null ? ((Number) row[0]).doubleValue() : 0.0);
            dashboard.put("tongDonHang", row[1] != null ? ((Number) row[1]).intValue() : 0);
            dashboard.put("giaTriDonTrungBinh", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
            dashboard.put("tongSanPhamBan", row[3] != null ? ((Number) row[3]).intValue() : 0);
            dashboard.put("donDangXuLy", row[4] != null ? ((Number) row[4]).intValue() : 0);
            dashboard.put("donDangGiao", row[5] != null ? ((Number) row[5]).intValue() : 0);
            dashboard.put("donHoanTat", row[6] != null ? ((Number) row[6]).intValue() : 0);
            dashboard.put("donBiTuChoi", row[7] != null ? ((Number) row[7]).intValue() : 0);
        }
        
        return dashboard;
    }

    /**
     * Thống kê theo danh mục sản phẩm
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<Map> với thống kê theo từng danh mục
     */
    public List<Map<String, Object>> thongKeTheoDanhMuc(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoDanhMuc(startDate, endDate);
        return mapToListMap(results, new String[]{"tenDanhMuc", "doanhThu", "soDonHang", "soSanPham", "soLoaiSanPham"});
    }

    /**
     * Top sản phẩm bán chạy nhất
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param limit Số lượng sản phẩm top
     * @return List<Map> với top sản phẩm
     */
    public List<Map<String, Object>> topSanPhamBanChay(Date startDate, Date endDate, int limit) {
        List<Object[]> results = thongKeDAO.topSanPhamBanChay(startDate, endDate, limit);
        return mapToListMap(results, new String[]{"maSP", "tenSP", "tongSoLuong", "tongDoanhThu", "soDonMua"});
    }

    /**
     * Top khách hàng mua nhiều nhất
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param limit Số lượng khách hàng top
     * @return List<Map> với top khách hàng
     */
    public List<Map<String, Object>> topKhachHang(Date startDate, Date endDate, int limit) {
        List<Object[]> results = thongKeDAO.topKhachHang(startDate, endDate, limit);
        return mapToListMap(results, new String[]{"maKH", "tenKH", "sdt", "soDonMua", "tongChiTieu"});
    }

    /**
     * Thống kê đơn hàng theo trạng thái
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<Map> với thống kê theo trạng thái
     */
    public List<Map<String, Object>> thongKeTheoTrangThai(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoTrangThai(startDate, endDate);
        return mapToListMap(results, new String[]{"trangThai", "soDon", "doanhThu"});
    }

    /**
     * Helper method để map Object[] sang List<Map>
     */
    private List<Map<String, Object>> mapToListMap(List<Object[]> results, String[] keys) {
        List<Map<String, Object>> list = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < keys.length; i++) {
                if (i < row.length) {
                    Object value = row[i];
                    if (value instanceof Number) {
                        map.put(keys[i], ((Number) value).doubleValue());
                    } else {
                        map.put(keys[i], value);
                    }
                }
            }
            list.add(map);
        }
        
        return list;
    }

    /**
     * Thống kê mặc định - 30 ngày gần nhất
     * 
     * @return Map chứa các thống kê mặc định
     */
    public Map<String, Object> thongKeMacDinh() {
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = cal.getTime();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tongQuan", thongKeTongQuan(startDate, endDate));
        result.put("theoNgay", thongKeTheoNgay(startDate, endDate));
        result.put("theoThang", thongKeTheoThang(startDate, endDate));
        result.put("theoDanhMuc", thongKeTheoDanhMuc(startDate, endDate));
        result.put("topSanPham", topSanPhamBanChay(startDate, endDate, 10));
        result.put("topKhachHang", topKhachHang(startDate, endDate, 5));
        result.put("donHangGanDay", getRecentOrders(5));
        
        return result;
    }

    /**
     * Lấy danh sách đơn hàng gần đây
     * 
     * @param limit Số lượng đơn hàng cần lấy
     * @return List<Map> danh sách đơn hàng gần đây
     */
    public List<Map<String, Object>> getRecentOrders(int limit) {
        List<Object[]> results = thongKeDAO.getRecentOrders(limit);
        return mapToListMap(results, new String[]{"maHD", "tenKH", "ngayMua", "tongTien", "trangThai"});
    }
}
