package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new DefaultOAuth2UserService();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.addAllowedOrigin("http://localhost:5173");
                config.addAllowedOrigin("http://localhost:8080");
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/auth/**", 
                    "/api/**", 
                    "/images/**",
                    "/anh/**", 
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .defaultSuccessUrl("http://localhost:5173/auth/login?googleSuccess=true", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauth2UserService())
                )
                .successHandler((request, response, authentication) -> {
                    
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    Map<String, Object> attributes = oauth2User.getAttributes();
                    
                    String email = (String) attributes.get("email");
                    String name = (String) attributes.get("name");
                    
                    if (email == null) {
                        email = (String) attributes.get("sub") + "@gmail.com";
                    }
                    
                    if (name == null) {
                        name = "Google User";
                    }
                    
                    String redirectUrl = "http://localhost:5173/auth/login?googleSuccess=true&email=" 
                        + URLEncoder.encode(email, StandardCharsets.UTF_8) 
                        + "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
                    
                    response.sendRedirect(redirectUrl);
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("http://localhost:5173/auth/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }
}