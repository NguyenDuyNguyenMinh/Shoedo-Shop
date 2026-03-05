// File: src/main/java/poly/edu/dto/ProductListDTO.java
package poly.edu.dto;

import lombok.Data;

@Data
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
}