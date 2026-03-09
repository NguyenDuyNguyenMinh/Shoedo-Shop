package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.QuanTriDAO;
import poly.edu.dao.UsersDAO;
import poly.edu.entity.QuanTri;
import poly.edu.entity.Users;
import poly.edu.service.AuthService;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired private AuthService authService;
    @Autowired private UsersDAO usersDAO;
    @Autowired private QuanTriDAO quanTriDAO;
    
    @GetMapping("/auto-login")
    public ResponseEntity<Map<String, Object>> autoLogin() {
        return ResponseEntity.ok(authService.autoLogin());
    }
    
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession() {
        return ResponseEntity.ok(authService.checkSession());
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.login(request));
    }
        
    @PostMapping("/send-register")
    public ResponseEntity<Map<String, Object>> sendRegister(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.sendRegister(request));
    }

    @PostMapping("/complete-register")
    public ResponseEntity<Map<String, Object>> completeRegister(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.completeRegister(request));
    }
  
    @PostMapping("/send-fg-pass")
    public ResponseEntity<Map<String, Object>> sendForgotPass(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.sendForgotPass(request));
    }
    
    @PostMapping("/confirm-fg-pass")
    public ResponseEntity<Map<String, Object>> confirmForgotPass(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.confirmForgotPass(request));
    }
    
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserInfo());
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(authService.logout());
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }
    
    @GetMapping("/cart-count")
    public ResponseEntity<Map<String, Object>> getCartCount() {
    	 Users currentUser = authService.getCurrentUser();
         if (currentUser == null) {
             return ResponseEntity.ok(Map.of("success", false, "count", 0));
         }
         
         Integer count = authService.getCartCount(currentUser);
         return ResponseEntity.ok(Map.of("success", true, "count", count));
    }
}
