package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.ChienDichDTO.*;
import poly.edu.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/chiendich")
public class ChienDichController {

    @Autowired
    private ChienDichService chienDichService;

    @PostMapping("/tao")
    public ResponseEntity<?> createCampaign(@RequestBody CampaignRequestDTO request) {
        try{
        chienDichService.createCampaign(request);
        return ResponseEntity.ok().body("Tạo chiến dịch thành công");
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/danh-sach")
    public ResponseEntity<List<CampaignResponseDTO>> getCampaigns() {
        return ResponseEntity.ok(chienDichService.getAllCampaigns());
    }

    @GetMapping("/{maCD}/chi-tiet")
    public ResponseEntity<List<CampaignDetailResponseDTO>> getCampaignDetails(@PathVariable Integer maCD) {
        return ResponseEntity.ok(chienDichService.getCampaignDetails(maCD));
    }

    @PutMapping("/{maCD}/ket-thuc-som")
    public ResponseEntity<?> endCampaignEarly(@PathVariable Integer maCD) {
        chienDichService.endCampaignEarly(maCD);
        return ResponseEntity.ok().body("Đã kết thúc chiến dịch");
    }

    @PutMapping("/{maCD}/cap-nhat-km")
    public ResponseEntity<?> updateCampaignDiscount(@PathVariable Integer maCD, @RequestBody ProductCampaignDTO payload) {
        return ResponseEntity.ok().body("Cập nhật phần trăm thành công");
    }
}