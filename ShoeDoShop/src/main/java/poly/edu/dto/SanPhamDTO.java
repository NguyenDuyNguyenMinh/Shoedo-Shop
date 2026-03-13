package poly.edu.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamDTO {

    private Integer maSP;
    private String tenSP;
    private Boolean gioiTinh;

    // ── Trang chủ ──
    private String hinhAnh;
    private Double giaGoc;
    private Double giaSauKM;
    private Integer tongSoLuong;
    private Integer daBan;

    // ── Admin list ──
    private String danhMucs;
    private int soPhanLoai;
    private Double giaDaiDien;
    private Double giaMin;
    private Double giaMax;
    private int tongTonKho;
    private String hinhAnhDaiDien;
    private String trangThai;
    private Boolean isActive;
    private Integer khuyenMai;
}
