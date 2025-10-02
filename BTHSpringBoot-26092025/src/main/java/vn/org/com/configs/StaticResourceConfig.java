package vn.org.com.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir; // ví dụ: "uploads"

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = root.toUri().toString(); // file:/.../uploads/

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(0);

        // (tuỳ chọn) alias
//        registry.addResourceHandler("/image/**")
//                .addResourceLocations(location)
//                .setCachePeriod(0);
    }
}
