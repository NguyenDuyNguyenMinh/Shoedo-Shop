package poly.edu.dto;
import java.math.BigDecimal;

public class SanPhamKhuyenMaiResponse {
    private Integer maSP;
    private String tenSP;
    private Integer daBan;
    private Integer khuyenMai;
    private String hinhAnh;
    private BigDecimal donGia;
    private Boolean isActive;

    // Constructor đầy đủ tham số để map dữ liệu
    public SanPhamKhuyenMaiResponse(Integer maSP, String tenSP, Integer daBan, Integer khuyenMai, String hinhAnh, BigDecimal donGia, Boolean isActive) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.daBan = daBan;
        this.khuyenMai = khuyenMai;
        this.hinhAnh = hinhAnh;
        this.donGia = donGia;
        this.isActive = isActive;
    }

    // --- Giữ lại các Getters và Setters cũ của bạn ---
    public Integer getMaSP() { return maSP; }
    public void setMaSP(Integer maSP) { this.maSP = maSP; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public Integer getDaBan() { return daBan; }
    public void setDaBan(Integer daBan) { this.daBan = daBan; }
    public Integer getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(Integer khuyenMai) { this.khuyenMai = khuyenMai; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}