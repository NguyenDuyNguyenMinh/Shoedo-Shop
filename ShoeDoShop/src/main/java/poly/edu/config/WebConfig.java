package poly.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
	@Bean
	public CorsFilter corsFilter() {
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    
	    config.setAllowCredentials(true);
	    config.addAllowedOrigin("http://localhost:5173");
	    config.addAllowedOrigin("http://localhost:5174");
	    config.addAllowedOrigin("http://localhost:8080");
	    config.addAllowedOrigin("http://localhost:4200");
	    config.addAllowedOrigin("https://sandbox.vnpayment.vn");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("*");
	    
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
        		.maxAge(3600);
    }
    
}