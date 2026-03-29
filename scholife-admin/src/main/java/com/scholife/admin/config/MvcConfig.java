package com.scholife.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Path where your images are actually saved on your laptop/server
        // If they are in a folder called 'uploads' in your project root:
        exposeDirectory("uploads", registry);
    }

    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(dirName);
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // This tells Spring: when you see /uploads/**, look in the physical folder
        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}