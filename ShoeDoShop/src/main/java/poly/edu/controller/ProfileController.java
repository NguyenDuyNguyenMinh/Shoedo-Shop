package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.ProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class ProfileController {
    
    @Autowired private ProfileService profileService;
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(profileService.changePassword(request));
    }
    
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses() {
        return ResponseEntity.ok(profileService.getAddresses());
    }
    
    @PostMapping("/address")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(profileService.addAddress(request));
    }
    
    @PutMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(profileService.updateAddress(id, request));
    }
    
    @DeleteMapping("/address/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Integer id) {
        return ResponseEntity.ok(profileService.deleteAddress(id));
    }
    
    @PostMapping("/address/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Integer id) {
        return ResponseEntity.ok(profileService.setDefaultAddress(id));
    }
    
    @GetMapping("/points-history")
    public ResponseEntity<Map<String, Object>> getPointsHistory() {
        return ResponseEntity.ok(profileService.getPointsHistory());
    }
    
    @GetMapping("/vouchers/my-vouchers")
    public ResponseEntity<Map<String, Object>> getMyVouchers() {
        return ResponseEntity.ok(profileService.getMyVouchers());
    }
    
    @GetMapping("/vouchers/available")
    public ResponseEntity<Map<String, Object>> getAvailableVouchers() {
        return ResponseEntity.ok(profileService.getAvailableVouchers());
    }
    
    @PostMapping("/vouchers/redeem")
    public ResponseEntity<Map<String, Object>> redeemVoucher(@RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(profileService.redeemVoucher(request.get("maVoucher")));
    }
    
    @PostMapping("/apply-referral")
    public ResponseEntity<Map<String, Object>> applyReferralCode(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(profileService.applyReferralCode(request.get("referralCode")));
    }
}