package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {
    
    @Autowired private AuthService authService;
    
    @GetMapping("/google/callback")
    public ResponseEntity<Map<String, Object>> googleCallback(@RequestParam String email, @RequestParam(required = false) String name) {
        return ResponseEntity.ok(authService.handleGoogleCallback(email, name));
    }
    
    @PostMapping("/google-login")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }
    
    @PostMapping("/google-login-new")
    public ResponseEntity<Map<String, Object>> googleLoginNew(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.googleLoginNew(request));
    }
}