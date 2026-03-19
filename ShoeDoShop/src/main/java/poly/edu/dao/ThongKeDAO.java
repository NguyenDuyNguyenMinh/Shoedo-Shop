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
     * Thống kê doanh thu theo ngày
     * Chỉ tính các đơn hàng đã hoàn tất
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<Object[]> với ngay, doanhThu, soDonHang, giaTriDonTB, soSP
     */
    @Query(value = """
            SELECT
                CAST(hd.NgayMua AS DATE) AS ngay,
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
            GROUP BY CAST(hd.NgayMua AS DATE)
            ORDER BY CAST(hd.NgayMua AS DATE)
            """, nativeQuery = true)
    List<Object[]> thongKeTheoNgay(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * Thống kê doanh thu theo tháng
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return List<Object[]> với thang, doanhThu, soDonHang, giaTriDonTB, soSP
     */
    @Query(value = """
            SELECT
                FORMAT(hd.NgayMua, 'yyyy-MM') AS thang,
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
            GROUP BY FORMAT(hd.NgayMua, 'yyyy-MM')
            ORDER BY FORMAT(hd.NgayMua, 'yyyy-MM')
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
                ISNULL(SUM(CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hdct.DonGia * hdct.SoLuong ELSE 0 END), 0) AS doanhThu,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hd.MaHD END) AS soDonHang,
                ISNULL(SUM(CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hdct.SoLuong ELSE 0 END), 0) AS soSP,
                COUNT(DISTINCT sp.MaSP) AS soSanPham
            FROM DanhMuc dm
            LEFT JOIN SanPham_DanhMuc spdm ON dm.MaDM = spdm.MaDM
            LEFT JOIN SanPham sp ON spdm.MaSP = sp.MaSP
            LEFT JOIN SanPham_ChiTiet spct ON sp.MaSP = spct.MaSP
            LEFT JOIN HoaDonCT hdct ON spct.MaSKU = hdct.MaSKU
            LEFT JOIN HoaDon hd ON hdct.MaHD = hd.MaHD
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
                -- Doanh thu chỉ tính đơn Hoàn tất
                ISNULL(SUM(CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hdct.DonGia * hdct.SoLuong ELSE 0 END), 0) AS tongDoanhThu,
                -- Tổng số đơn (tất cả trạng thái)
                COUNT(DISTINCT hd.MaHD) AS tongDonHang,
                -- Giá trị đơn TB (chỉ đơn Hoàn tất)
                CASE
                    WHEN COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hd.MaHD END) > 0
                    THEN ISNULL(SUM(CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hdct.DonGia * hdct.SoLuong ELSE 0 END), 0)
                        / COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hd.MaHD END)
                    ELSE 0
                END AS giaTriDonTB,
                -- Số sản phẩm bán (tổng SoLuong trong HoaDonCT theo date range, chỉ Hoàn tất)
                ISNULL(SUM(CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hdct.SoLuong ELSE 0 END), 0) AS tongSP,
                -- Đếm theo từng trạng thái
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Đang xử lý' THEN hd.MaHD END) AS donDangXuLy,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Đang giao' THEN hd.MaHD END) AS donDangGiao,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Hoàn tất' THEN hd.MaHD END) AS donHoanTat,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Đã từ chối' THEN hd.MaHD END) AS donBiTuChoi,
                COUNT(DISTINCT CASE WHEN hd.TrangThai = N'Báo lỗi' THEN hd.MaHD END) AS donBaoLoi
            FROM HoaDon hd
            LEFT JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
            WHERE hd.NgayMua BETWEEN :startDate AND :endDate
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
                spctOuter.MaSP AS MaSP,
                sp.TenSP,
                ISNULL((
                    SELECT SUM(hdct.SoLuong)
                    FROM HoaDonCT hdct
                    INNER JOIN HoaDon hd ON hdct.MaHD = hd.MaHD
                    WHERE hd.TrangThai = N'Hoàn tất'
                        AND hd.NgayMua BETWEEN :startDate AND :endDate
                        AND hdct.MaSKU IN (SELECT MaSKU FROM SanPham_ChiTiet WHERE MaSP = sp.MaSP)
                ), 0) AS tongSoLuong,
                ISNULL((
                    SELECT SUM(hdct.DonGia * hdct.SoLuong)
                    FROM HoaDonCT hdct
                    INNER JOIN HoaDon hd ON hdct.MaHD = hd.MaHD
                    WHERE hd.TrangThai = N'Hoàn tất'
                        AND hd.NgayMua BETWEEN :startDate AND :endDate
                        AND hdct.MaSKU IN (SELECT MaSKU FROM SanPham_ChiTiet WHERE MaSP = sp.MaSP)
                ), 0) AS tongDoanhThu,
                (SELECT COUNT(DISTINCT hd.MaHD)
                    FROM HoaDon hd
                    INNER JOIN HoaDonCT hdct ON hd.MaHD = hdct.MaHD
                    INNER JOIN SanPham_ChiTiet spct2 ON hdct.MaSKU = spct2.MaSKU
                    WHERE hd.TrangThai = N'Hoàn tất'
                        AND hd.NgayMua BETWEEN :startDate AND :endDate
                        AND spct2.MaSP = sp.MaSP) AS soDonMua
            FROM SanPham sp
            INNER JOIN SanPham_ChiTiet spctOuter ON sp.MaSP = spctOuter.MaSP
            WHERE EXISTS (
                SELECT 1 FROM HoaDonCT hdct
                INNER JOIN HoaDon hd ON hdct.MaHD = hd.MaHD
                INNER JOIN SanPham_ChiTiet spct3 ON hdct.MaSKU = spct3.MaSKU
                WHERE hd.TrangThai = N'Hoàn tất'
                    AND hd.NgayMua BETWEEN :startDate AND :endDate
                    AND spct3.MaSP = sp.MaSP
            )
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
                (SELECT COUNT(*) FROM HoaDon hd2 WHERE hd2.MaKH = kh.MaKH AND hd2.NgayMua BETWEEN :startDate AND :endDate) AS soDonMua,
                ISNULL((SELECT SUM(hdct.DonGia * hdct.SoLuong)
                    FROM HoaDon hd3
                    INNER JOIN HoaDonCT hdct ON hd3.MaHD = hdct.MaHD
                    WHERE hd3.MaKH = kh.MaKH
                        AND hd3.TrangThai = N'Hoàn tất'
                        AND hd3.NgayMua BETWEEN :startDate AND :endDate), 0) AS tongChiTieu
            FROM KhachHang kh
            WHERE EXISTS (SELECT 1 FROM HoaDon hd4 WHERE hd4.MaKH = kh.MaKH AND hd4.NgayMua BETWEEN :startDate AND :endDate)
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
