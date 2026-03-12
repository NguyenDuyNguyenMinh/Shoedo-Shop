package poly.edu.dto;
import java.math.BigDecimal;

public class SanPhamKhuyenMaiResponse {
    private Integer maSP;
    private String tenSP;
    private Integer daBan;
    private Integer khuyenMai;
    private String hinhAnh;
    private BigDecimal donGiaMin;
    private BigDecimal donGiaMax;
    private Boolean isActive;
    private String maDMs;

    // Constructor đầy đủ tham số để map dữ liệu
    public SanPhamKhuyenMaiResponse(Integer maSP, String tenSP, Integer daBan, Integer khuyenMai, String hinhAnh, BigDecimal donGiaMin, BigDecimal donGiaMax, Boolean isActive, String maDMs) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.daBan = daBan;
        this.khuyenMai = khuyenMai;
        this.hinhAnh = hinhAnh;
        this.donGiaMin = donGiaMin;
        this.donGiaMax = donGiaMax;
        this.isActive = isActive;
        this.maDMs = maDMs;
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
    public BigDecimal getDonGiaMin() { return donGiaMin; }
    public void setDonGiaMin(BigDecimal donGiaMin) { this.donGiaMin = donGiaMin; }
    public BigDecimal getDonGiaMax() { return donGiaMax; }
    public void setDonGiaMax(BigDecimal donGiaMax) { this.donGiaMax = donGiaMax; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getMaDMs() { return maDMs; }
    public void setMaDMs(String maDMs) { this.maDMs = maDMs; }
}