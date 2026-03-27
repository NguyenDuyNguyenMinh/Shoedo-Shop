package poly.edu.service;

import poly.edu.dto.ChienDichDTO.*;


import java.util.List;

public interface ChienDichService {
    
    // Tạo chiến dịch mới
    void createCampaign(CampaignRequestDTO request);

    // Lấy danh sách tất cả chiến dịch (đã gom nhóm theo tên)
    List<CampaignResponseDTO> getAllCampaigns();

    // Lấy chi tiết các sản phẩm trong một chiến dịch cụ thể
    List<CampaignDetailResponseDTO> getCampaignDetails(Integer maCD);

    // Kết thúc sớm một chiến dịch
    void endCampaignEarly(Integer maCD);

    // Cập nhật % khuyến mãi cho 1 sản phẩm đang trong chiến dịch
    void updateCampaignDiscount(Integer maCD, ProductCampaignDTO payload);
}