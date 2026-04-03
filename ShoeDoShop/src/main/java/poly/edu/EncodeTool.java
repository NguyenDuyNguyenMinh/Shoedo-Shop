package poly.edu;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncodeTool {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123");
        System.out.println("BCrypt hash of '123': " + hash);
        System.out.println("Verify: " + encoder.matches("123", hash));
    }
}
