package poly.edu.dto;

import lombok.Data;
import java.util.Date;

@Data
public class QLDanhGiaDTO {
    private Integer maDG;
    private Integer sao;
    private String danhGiaCT;
    private Date ngayDG;
    
    // Thông tin từ HoaDonCT
    private Integer maHDCT;
    private Double donGia;
    private Integer soLuong;
    
    // Thông tin từ HoaDon
    private Integer maHD;
    private String phuongThucTT;
    private String trangThaiHD;
    private Date ngayMua;
    
    // Thông tin voucher được áp dụng
    private Double giaTriGiam;        // Số tiền được giảm từ voucher
    private String tenVoucher;        // Tên voucher đã áp dụng
    private Double donGiaSauGiam;     // Giá sau khi giảm (giá thực tế đã mua)
    private Double donToiThieu;        // Đơn tối thiểu để áp dụng voucher
    
    // Thông tin từ KhachHang
    private Integer maKH;
    private String tenKH;
    private String sdt;
    
    // Thông tin từ User
    private String userName;
    private String mail;
    
    // Thông tin từ SanPhamChiTiet
    private Integer maSKU;
    private String tenMau;
    private String hinhAnh;
    
    // Thông tin từ SanPham
    private Integer maSP;
    private String tenSP;
    private Boolean gioiTinh;
    
    // Thông tin từ Size
    private Integer maSize;
    private Integer coGiay;
}