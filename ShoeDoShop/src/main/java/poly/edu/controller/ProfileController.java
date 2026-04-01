package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.*;
import poly.edu.service.ProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class ProfileController {
    
    @Autowired
    private ProfileService profileService;
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        try {
            return ResponseEntity.ok(profileService.getProfile());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(profileService.updateProfile(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(profileService.changePassword(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses() {
        try {
            return ResponseEntity.ok(profileService.getAddresses());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/address")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(profileService.addAddress(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PutMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(profileService.updateAddress(id, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(profileService.deleteAddress(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/address/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(profileService.setDefaultAddress(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/points-history")
    public ResponseEntity<Map<String, Object>> getPointsHistory() {
        try {
            return ResponseEntity.ok(profileService.getPointsHistory());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/vouchers/my-vouchers")
    public ResponseEntity<Map<String, Object>> getMyVouchers() {
        try {
            return ResponseEntity.ok(profileService.getMyVouchers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/vouchers/available")
    public ResponseEntity<Map<String, Object>> getAvailableVouchers() {
        try {
            return ResponseEntity.ok(profileService.getAvailableVouchers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/vouchers/redeem")
    public ResponseEntity<Map<String, Object>> redeemVoucher(@RequestBody Map<String, Integer> request) {
        try {
            Integer maVoucher = request.get("maVoucher");
            return ResponseEntity.ok(profileService.redeemVoucher(maVoucher));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/apply-referral")
    public ResponseEntity<Map<String, Object>> applyReferralCode(@RequestBody Map<String, String> request) {
        try {
            String referralCode = request.get("referralCode");
            return ResponseEntity.ok(profileService.applyReferralCode(referralCode));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}