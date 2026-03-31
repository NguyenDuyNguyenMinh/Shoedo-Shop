package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dto.ChienDichDTO.*;
import poly.edu.entity.ChienDich;
import poly.edu.entity.SanPham;
import poly.edu.dao.ChienDichDAO;
import poly.edu.dao.SanPhamDAO;
import poly.edu.service.ChienDichService;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChienDichServiceImpl implements ChienDichService {
	@Autowired
    private ChienDichDAO chienDichRepository;
    
    @Autowired
    private SanPhamDAO sanPhamRepository;

    @Override
    @Transactional
    public void createCampaign(CampaignRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();

        String initialStatus = request.getThoiGianBatDau().isAfter(now) ? "Chưa bắt đầu" : "Đang chạy";

        for (ProductCampaignDTO spDto : request.getSanPhams()) {
            SanPham sp = sanPhamRepository.findById(spDto.getMaSP())
                            .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy SP!"));

            ChienDich cd = new ChienDich();
            cd.setTenChienDich(request.getTenChienDich());
            cd.setThoiGianBatDau(request.getThoiGianBatDau());
            cd.setThoiGianKetThuc(request.getThoiGianKetThuc());
            cd.setKhuyenMaiCD(spDto.getKhuyenMai());
            cd.setTrangThai(initialStatus);
            cd.setSanPham(sp); 
            
            chienDichRepository.save(cd);

            if ("Đang chạy".equals(initialStatus)) {
                sp.setKhuyenMai(spDto.getKhuyenMai());
                sanPhamRepository.save(sp);
            }
        }
    }

    @Override
    public List<CampaignResponseDTO> getAllCampaigns() {
        return chienDichRepository.getCampaignHistory();
    }

    @Override
    public List<CampaignDetailResponseDTO> getCampaignDetails(Integer maCD) {
        ChienDich cd = chienDichRepository.findById(maCD).orElseThrow();
        List<Object[]> listNative = chienDichRepository.getCampaignDetailsNative(cd.getTenChienDich());
        
        List<CampaignDetailResponseDTO> details = new ArrayList<>();
        for (Object[] row : listNative) {
            CampaignDetailResponseDTO dto = new CampaignDetailResponseDTO();
            dto.setMaSP((Integer) row[0]);
            dto.setTenSP((String) row[1]);
            dto.setHinhAnh((String) row[2]);
            dto.setKhuyenMai(row[3] != null ? (Integer) row[3] : 0);
            dto.setDonGiaMin(row[4] != null ? Double.valueOf(row[4].toString()) : 0.0);
            dto.setDonGiaMax(row[5] != null ? Double.valueOf(row[5].toString()) : 0.0);
            details.add(dto);
        }
        return details;
    }

    @Override
    @Transactional
    public void endCampaignEarly(Integer maCD) {
        ChienDich cdGroup = chienDichRepository.findById(maCD).orElseThrow();
        List<ChienDich> listCD = chienDichRepository.findByTenChienDich(cdGroup.getTenChienDich());
        
        for (ChienDich item : listCD) {
            item.setTrangThai("Kết thúc");
            chienDichRepository.save(item);
            
            SanPham sp = item.getSanPham();
            if (sp != null) {
                sp.setKhuyenMai(0);
                sanPhamRepository.save(sp);
            }
        }
    }
    

    @Override
    @Transactional
    public void updateCampaignDiscount(Integer maCD, ProductCampaignDTO payload) {
        ChienDich cdGroup = chienDichRepository.findById(maCD).orElseThrow();
        List<ChienDich> listCD = chienDichRepository.findByTenChienDich(cdGroup.getTenChienDich());
        
        for (ChienDich cd : listCD) {
            if (cd.getSanPham().getMaSP().equals(payload.getMaSP())) {
                
                cd.setKhuyenMaiCD(payload.getKhuyenMai()); 
                chienDichRepository.save(cd);
                

                if ("Đang chạy".equals(cd.getTrangThai())) {
                    SanPham sp = cd.getSanPham();
                    sp.setKhuyenMai(payload.getKhuyenMai());
                    sanPhamRepository.save(sp);
                }
                break;
            }
        }
    }
    
    @Scheduled(fixedRate = 10000) 
    @Transactional
    public void autoCheckAndEndCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        List<ChienDich> allCampaigns = chienDichRepository.findAll();
        
        for (ChienDich cd : allCampaigns) {
            SanPham sp = cd.getSanPham();
            

            if ("Chưa bắt đầu".equals(cd.getTrangThai()) && !now.isBefore(cd.getThoiGianBatDau())) {
                cd.setTrangThai("Đang chạy");
                chienDichRepository.save(cd);
                
                if (sp != null) {
                    sp.setKhuyenMai(cd.getKhuyenMaiCD()); 
                    sanPhamRepository.save(sp);
                }
                System.out.println("Đã tự động BẮT ĐẦU chiến dịch: " + cd.getTenChienDich());
            }
            

            else if ("Đang chạy".equals(cd.getTrangThai()) && now.isAfter(cd.getThoiGianKetThuc())) {
                cd.setTrangThai("Kết thúc");
                chienDichRepository.save(cd);
                
                if (sp != null) {
                    sp.setKhuyenMai(0); 
                    sanPhamRepository.save(sp);
                }
                System.out.println("Đã tự động KẾT THÚC chiến dịch: " + cd.getTenChienDich());
            }
        }
    }
}