package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.ThongKeDAO;
import poly.edu.dto.ThongKeDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// #region DEBUG - ThongKeService logging
import java.io.FileWriter;
import java.io.PrintWriter;
// #endregion

@Service
public class ThongKeService {

    @Autowired
    private ThongKeDAO thongKeDAO;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

    /**
     * Thống kê doanh thu theo ngày
     * Bao gồm fill missing dates (các ngày không có đơn hàng sẽ có giá trị 0)
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với doanh thu, số đơn hàng, giá trị đơn TB, số sản phẩm theo ngày
     */
    public List<ThongKeDTO> thongKeTheoNgay(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoNgay(startDate, endDate);
        List<ThongKeDTO> dtos = mapResultsToThongKeDTO(results, "day");
        
        // Fill missing dates
        return fillMissingDates(dtos, startDate, endDate);
    }

    /**
     * Điền các ngày còn thiếu trong khoảng thời gian
     * 
     * @param dtos Danh sách thống kê đã có
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với đầy đủ các ngày
     */
    private List<ThongKeDTO> fillMissingDates(List<ThongKeDTO> dtos, Date startDate, Date endDate) {
        // Tạo map từ kết quả có sẵn
        Map<Date, ThongKeDTO> existingData = new LinkedHashMap<>();
        for (ThongKeDTO dto : dtos) {
            if (dto.getThoiGian() != null) {
                existingData.put(normalizeDate(dto.getThoiGian()), dto);
            }
        }
        
        // Tạo danh sách đầy đủ
        List<ThongKeDTO> fullList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        Date current = cal.getTime();
        Date end = normalizeDate(endDate);
        
        while (!current.after(end)) {
            Date normalizedCurrent = normalizeDate(current);
            if (existingData.containsKey(normalizedCurrent)) {
                fullList.add(existingData.get(normalizedCurrent));
            } else {
                // Thêm ngày không có dữ liệu với giá trị 0
                ThongKeDTO emptyDto = new ThongKeDTO();
                emptyDto.setThoiGian(normalizedCurrent);
                emptyDto.setDoanhThu(0.0);
                emptyDto.setSoDonHang(0);
                emptyDto.setGiaTriDonTrungBinh(0.0);
                emptyDto.setSoSanPhamBanRa(0);
                fullList.add(emptyDto);
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
            current = cal.getTime();
        }
        
        return fullList;
    }

    /**
     * Chuẩn hóa ngày về định dạng yyyy-MM-dd (bỏ giờ phút giây)
     */
    private Date normalizeDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Thống kê doanh thu theo tháng
     * Bao gồm fill missing months (các tháng không có đơn hàng sẽ có giá trị 0)
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với doanh thu, số đơn hàng, giá trị đơn TB, số sản phẩm theo tháng
     */
    public List<ThongKeDTO> thongKeTheoThang(Date startDate, Date endDate) {
        List<Object[]> results = thongKeDAO.thongKeTheoThang(startDate, endDate);
        List<ThongKeDTO> dtos = mapResultsToThongKeDTO(results, "month");
        
        // Fill missing months
        return fillMissingMonths(dtos, startDate, endDate);
    }

    /**
     * Điền các tháng còn thiếu trong khoảng thời gian
     * 
     * @param dtos Danh sách thống kê đã có
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<ThongKeDTO> với đầy đủ các tháng
     */
    private List<ThongKeDTO> fillMissingMonths(List<ThongKeDTO> dtos, Date startDate, Date endDate) {
        // Tạo map từ kết quả có sẵn (key là yyyy-MM)
        Map<String, ThongKeDTO> existingData = new LinkedHashMap<>();
        for (ThongKeDTO dto : dtos) {
            if (dto.getThoiGian() != null) {
                String monthKey = monthFormat.format(dto.getThoiGian());
                existingData.put(monthKey, dto);
            }
        }
        
        // Tạo danh sách đầy đủ
        List<ThongKeDTO> fullList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.DAY_OF_MONTH, 1); // Đầu tháng
        
        while (!cal.after(endCal)) {
            String monthKey = monthFormat.format(cal.getTime());
            if (existingData.containsKey(monthKey)) {
                fullList.add(existingData.get(monthKey));
            } else {
                // Thêm tháng không có dữ liệu với giá trị 0
                ThongKeDTO emptyDto = new ThongKeDTO();
                emptyDto.setThoiGian(cal.getTime());
                emptyDto.setDoanhThu(0.0);
                emptyDto.setSoDonHang(0);
                emptyDto.setGiaTriDonTrungBinh(0.0);
                emptyDto.setSoSanPhamBanRa(0);
                fullList.add(emptyDto);
            }
            cal.add(Calendar.MONTH, 1);
        }
        
        return fullList;
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
        // #region DEBUG - Log input params
        try {
            String logPath = "c:\\Users\\dothanhphong\\Downloads\\Shoedo-Shop\\.cursor\\debug.log";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String msg = "thongKeTongQuan called: startDate=" + sdf.format(startDate) + ", endDate=" + sdf.format(endDate);
            try (FileWriter fw = new FileWriter(logPath, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println("{\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ThongKeService:thongKeTongQuan\",\"message\":\"" + msg + "\",\"hypothesisId\":\"H2\"}");
            }
        } catch (Exception e) {}
        // #endregion
        
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
            
            // #region DEBUG - Log output
            try {
                String logPath = "c:\\Users\\dothanhphong\\Downloads\\Shoedo-Shop\\.cursor\\debug.log";
                String msg = "thongKeTongQuan result: tongDoanhThu=" + dashboard.get("tongDoanhThu") 
                    + ", tongDonHang=" + dashboard.get("tongDonHang")
                    + ", donDangXuLy=" + dashboard.get("donDangXuLy")
                    + ", donDangGiao=" + dashboard.get("donDangGiao")
                    + ", donHoanTat=" + dashboard.get("donHoanTat")
                    + ", donBiTuChoi=" + dashboard.get("donBiTuChoi");
                try (FileWriter fw = new FileWriter(logPath, true);
                     PrintWriter pw = new PrintWriter(fw)) {
                    pw.println("{\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ThongKeService:thongKeTongQuan\",\"message\":\"" + msg + "\",\"hypothesisId\":\"H1\"}");
                }
            } catch (Exception e) {}
            // #endregion
        } else {
            // #region DEBUG - Log empty results
            try {
                String logPath = "c:\\Users\\dothanhphong\\Downloads\\Shoedo-Shop\\.cursor\\debug.log";
                String msg = "thongKeTongQuan: NO RESULTS returned";
                try (FileWriter fw = new FileWriter(logPath, true);
                     PrintWriter pw = new PrintWriter(fw)) {
                    pw.println("{\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"ThongKeService:thongKeTongQuan\",\"message\":\"" + msg + "\",\"hypothesisId\":\"H1\"}");
                }
            } catch (Exception e) {}
            // #endregion
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
