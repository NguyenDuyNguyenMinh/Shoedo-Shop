package poly.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig CHỈ xử lý static resource.
 *
 * CORS đã được xử lý hoàn toàn trong SecurityConfig (.cors(...))
 * → KHÔNG được có CorsFilter @Bean hoặc addCorsMappings() ở đây
 *   vì sẽ tạo ra 2 CorsFilter chạy song song → conflict wildcard "*"
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}