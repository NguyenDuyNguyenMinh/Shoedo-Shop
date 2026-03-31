package poly.edu.service;

import poly.edu.dto.ChienDichDTO.*;


import java.util.List;

public interface ChienDichService {

    void createCampaign(CampaignRequestDTO request);

    List<CampaignResponseDTO> getAllCampaigns();

    List<CampaignDetailResponseDTO> getCampaignDetails(Integer maCD);

    void endCampaignEarly(Integer maCD);

    void updateCampaignDiscount(Integer maCD, ProductCampaignDTO payload);
}