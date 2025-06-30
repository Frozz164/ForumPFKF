package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import java.io.File;

@Configuration
public class MultipartConfig {
    
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${spring.servlet.multipart.location}")
    private String tempDir;

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true); // Включаем ленивую загрузку
        return resolver;
    }

    @Bean
    public void createUploadDirectories() {
        // Создаем директорию для загрузок
        new File(uploadDir).mkdirs();
        // Создаем временную директорию
        new File(tempDir).mkdirs();
    }
} 