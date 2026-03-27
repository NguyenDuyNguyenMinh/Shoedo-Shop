package poly.edu.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

public class ChienDichDTO {
	@Data
    public static class CampaignRequestDTO {
        private String tenChienDich;
        private LocalDateTime thoiGianBatDau;
        private LocalDateTime thoiGianKetThuc;
        private List<ProductCampaignDTO> sanPhams;
    }

    // 2. DTO hứng từng sản phẩm trong Request
    @Data
    public static class ProductCampaignDTO {
        private Integer maSP;
        private Integer khuyenMai;
    }

    // 3. DTO trả về danh sách lịch sử (đã group)
    @Data
    public static class CampaignResponseDTO {
        private Integer maCD; // Lấy MaCD đầu tiên làm đại diện
        private String tenChienDich;
        private LocalDateTime thoiGianBatDau;
        private LocalDateTime thoiGianKetThuc;
        private String trangThai;
        
        public CampaignResponseDTO(Integer maCD, String tenChienDich, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc, String trangThai) {
            this.maCD = maCD;
            this.tenChienDich = tenChienDich;
            this.thoiGianBatDau = thoiGianBatDau;
            this.thoiGianKetThuc = thoiGianKetThuc;
            this.trangThai = trangThai;
        }
    }

    // 4. DTO trả về chi tiết sản phẩm trong chiến dịch (cho Accordion)
    @Data
    public static class CampaignDetailResponseDTO {
        private Integer maSP;
        private String tenSP;
        private String hinhAnh;
        private Integer khuyenMai; // Lấy từ bảng SanPham
        private Double donGiaMin;  // Mock hoặc query từ SanPham_ChiTiet
        private Double donGiaMax;
    }
	
}
