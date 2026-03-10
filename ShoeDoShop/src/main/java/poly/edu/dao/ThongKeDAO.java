package poly.edu.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.HoaDon;

import java.util.Date;
import java.util.List;

@Repository
public interface ThongKeDAO extends JpaRepository<HoaDon, Integer> {

    /**
     * Thống kê doanh thu theo ngày với đầy đủ ngày (fill missing with zero)
     * Chỉ tính các đơn hàng đã hoàn tất
     * 
     * Time Complexity: O(n) where n = số ngày trong khoảng thời gian
     * Database: 1 query với GROUP BY trên DATE(NgayMua)
     */
    @Query(value = """
            SELECT
                AllDates.DateValue AS ngay,
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS doanhThu,
                COUNT(DISTINCT hd.MaHD) AS soDonHang,
                CASE
                    WHEN COUNT(DISTINCT hd.MaHD) > 0
                    THEN SUM(hdct.DonGia * hdct.SoLuong) / COUNT(DISTINCT hd.MaHD)
                    ELSE 0
                END AS giaTriDonTB,
                SUM(hdct.SoLuong) AS soSP
            FROM dbo.GetDatesBetween(:startDate, :endDate) AllDates
            LEFT JOIN HoaDon hd ON CAST(hd.NgayMua AS DATE) = AllDates.DateValue
                AND hd.TrangThai = N'Hoàn tất'
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            GROUP BY AllDates.DateValue
            ORDER BY AllDates.DateValue
            """, nativeQuery = true)
    List<Object[]> thongKeTheoNgay(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Thống kê doanh thu theo tháng với đầy đủ tháng (fill missing with zero)
     * 
     * Time Complexity: O(n) where n = số tháng trong khoảng thời gian
     */
    @Query(value = """
            SELECT
                AllMonths.MonthValue AS thang,
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS doanhThu,
                COUNT(DISTINCT hd.MaHD) AS soDonHang,
                CASE
                    WHEN COUNT(DISTINCT hd.MaHD) > 0
                    THEN SUM(hdct.DonGia * hdct.SoLuong) / COUNT(DISTINCT hd.MaHD)
                    ELSE 0
                END AS giaTriDonTB,
                SUM(hdct.SoLuong) AS soSP
            FROM dbo.GetMonthsBetween(:startDate, :endDate) AllMonths
            LEFT JOIN HoaDon hd ON FORMAT(hd.NgayMua, 'yyyy-MM') = AllMonths.MonthValue
                AND hd.TrangThai = N'Hoàn tất'
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            GROUP BY AllMonths.MonthValue
            ORDER BY AllMonths.MonthValue
            """, nativeQuery = true)
    List<Object[]> thongKeTheoThang(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Thống kê doanh thu theo năm
     * 
     * Time Complexity: O(n) where n = số năm trong khoảng thời gian
     */
    @Query(value = """
            SELECT
                YEAR(hd.NgayMua) AS nam,
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS doanhThu,
                COUNT(DISTINCT hd.MaHD) AS soDonHang,
                CASE
                    WHEN COUNT(DISTINCT hd.MaHD) > 0
                    THEN SUM(hdct.DonGia * hdct.SoLuong) / COUNT(DISTINCT hd.MaHD)
                    ELSE 0
                END AS giaTriDonTB,
                SUM(hdct.SoLuong) AS soSP
            FROM HoaDon hd
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            WHERE hd.TrangThai = N'Hoàn tất'
                AND hd.NgayMua BETWEEN :startDate AND :endDate
            GROUP BY YEAR(hd.NgayMua)
            ORDER BY YEAR(hd.NgayMua)
            """, nativeQuery = true)
    List<Object[]> thongKeTheoNam(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Thống kê doanh thu theo danh mục sản phẩm
     * 
     * Time Complexity: O(n) where n = số danh mục
     * Sử dụng JOIN thay vì N+1 queries
     */
    @Query(value = """
            SELECT
                dm.TenDM AS tenDanhMuc,
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS doanhThu,
                COUNT(DISTINCT hd.MaHD) AS soDonHang,
                SUM(hdct.SoLuong) AS soSP,
                COUNT(DISTINCT sp.MaSP) AS soSanPham
            FROM DanhMuc dm
            LEFT JOIN SanPham_DanhMuc spdm ON dm.MaDM = spdm.MaDM
            LEFT JOIN SanPham sp ON spdm.MaSP = sp.MaSP
            LEFT JOIN SanPham_ChiTiet spct ON sp.MaSP = spct.MaSP
            LEFT JOIN HoaDonCT hdct ON spct.MaSKU = hdct.MaSKU
            LEFT JOIN HoaDon hd ON hdct.MaHD = hd.MaHD
                AND hd.TrangThai = N'Hoàn tất'
                AND hd.NgayMua BETWEEN :startDate AND :endDate
            GROUP BY dm.MaDM, dm.TenDM
            ORDER BY doanhThu DESC
            """, nativeQuery = true)
    List<Object[]> thongKeTheoDanhMuc(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Tổng quan thống kê (Dashboard overview)
     * 
     * Time Complexity: O(1) - 1 query với aggregation
     */
    @Query(value = """
            SELECT
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS tongDoanhThu,
                COUNT(DISTINCT hd.MaHD) AS tongDonHang,
                CASE
                    WHEN COUNT(DISTINCT hd.MaHD) > 0
                    THEN SUM(hdct.DonGia * hdct.SoLuong) / COUNT(DISTINCT hd.MaHD)
                    ELSE 0
                END AS giaTriDonTB,
                SUM(hdct.SoLuong) AS tongSP,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Đang xử lý' THEN hd.MaHD END) AS donDangXuLy,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Đang giao' THEN hd.MaHD END) AS donDangGiao,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hd.MaHD END) AS donHoanTat,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Đã từ chối' THEN hd.MaHD END) AS donBiTuChoi
            FROM HoaDon hd
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            WHERE hd.TrangThai = N'Hoàn tất'
                AND hd.NgayMua BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<Object[]> thongKeTongQuan(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Top sản phẩm bán chạy nhất
     * 
     * Time Complexity: O(n log n) where n = số sản phẩm, do ORDER BY
     */
    @Query(value = """
            SELECT TOP (:limit)
                sp.MaSP,
                sp.TenSP,
                SUM(hdct.SoLuong) AS tongSoLuong,
                SUM(hdct.DonGia * hdct.SoLuong) AS tongDoanhThu,
                COUNT(DISTINCT hd.MaHD) AS soDonMua
            FROM SanPham sp
            INNER JOIN SanPham_ChiTiet spct ON sp.MaSP = spct.MaSP
            INNER JOIN HoaDonCT hdct ON spct.MaSKU = hdct.MaSKU
            INNER JOIN HoaDon hd ON hdct.MaHD = hd.MaHD
                AND hd.TrangThai = N'Hoàn tất'
                AND hd.NgayMua BETWEEN :startDate AND :endDate
            GROUP BY sp.MaSP, sp.TenSP
            ORDER BY tongSoLuong DESC
            """, nativeQuery = true)
    List<Object[]> topSanPhamBanChay(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("limit") Integer limit);

    /**
     * Top khách hàng mua nhiều nhất
     * 
     * Time Complexity: O(n log n)
     */
    @Query(value = """
            SELECT TOP (:limit)
                kh.MaKH,
                kh.TenKH,
                kh.SDT,
                COUNT(DISTINCT hd.MaHD) AS soDonMua,
                SUM(hdct.DonGia * hdct.SoLuong) AS tongChiTieu
            FROM KhachHang kh
            LEFT JOIN HoaDon hd ON kh.MaKH = hd.MaKH
                AND hd.TrangThai = N'Hoàn tất'
                AND hd.NgayMua BETWEEN :startDate AND :endDate
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            GROUP BY kh.MaKH, kh.TenKH, kh.SDT
            HAVING COUNT(DISTINCT hd.MaHD) > 0
            ORDER BY tongChiTieu DESC
            """, nativeQuery = true)
    List<Object[]> topKhachHang(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("limit") Integer limit);

    /**
     * Thống kê đơn hàng theo trạng thái trong khoảng thời gian
     * 
     * Time Complexity: O(1)
     */
    @Query(value = """
            SELECT
                hd.TrangThai AS trangThai,
                COUNT(*) AS soDon,
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS doanhThu
            FROM HoaDon hd
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            WHERE hd.NgayMua BETWEEN :startDate AND :endDate
            GROUP BY hd.TrangThai
            """, nativeQuery = true)
    List<Object[]> thongKeTheoTrangThai(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Lấy danh sách đơn hàng gần đây
     * 
     * Time Complexity: O(n)
     */
    @Query(value = """
            SELECT TOP (:limit)
                hd.MaHD AS maHD,
                kh.TenKH AS tenKH,
                hd.NgayMua AS ngayMua,
                ISNULL(SUM(hdct.DonGia * hdct.SoLuong), 0) AS tongTien,
                hd.TrangThai AS trangThai
            FROM HoaDon hd
            LEFT JOIN KhachHang kh ON hd.MaKH = kh.MaKH
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            GROUP BY hd.MaHD, kh.TenKH, hd.NgayMua, hd.TrangThai
            ORDER BY hd.NgayMua DESC
            """, nativeQuery = true)
    List<Object[]> getRecentOrders(@Param("limit") Integer limit);
}
