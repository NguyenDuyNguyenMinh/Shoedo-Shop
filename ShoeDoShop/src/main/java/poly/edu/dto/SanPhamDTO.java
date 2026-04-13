package poly.edu.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDTO {
    private Integer maSP;
    private String tenSP;
    private String danhMucs;
    private Boolean gioiTinh;
    private int soPhanLoai;
    private Double giaDaiDien;
    
    private Double giaMin;
    private Double giaMax;
    private Integer khuyenMai;
    
    private int tongTonKho;
    private String hinhAnhDaiDien;
    private String trangThai;
    private Boolean isActive;

    private String  hinhAnh;
    private Double  giaGoc;
    private Double  giaSauKM;
    private int     tongSoLuong;
    private int     daBan;
}
