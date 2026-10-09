package skyware.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Path to the root 'public' folder on your server
        String uploadPath = new File("public/").getAbsolutePath();

        // This maps: http://localhost:8087/public/** // To the physical 'public' folder on your Hostinger disk
        registry.addResourceHandler("/public/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
