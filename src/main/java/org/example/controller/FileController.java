package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Создаем директорию, если не существует
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Генерируем уникальное имя файла
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;
            Path targetPath = Paths.get(uploadDir).resolve(newFileName);

            // Сохраняем файл с использованием REPLACE_EXISTING
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Формируем URL для доступа к файлу
            String fileUrl = "/uploads/" + newFileName;

            log.info("Файл успешно загружен: {}", fileUrl);

            return ResponseEntity.ok(new FileUploadResponse(fileUrl));
        } catch (IOException e) {
            log.error("Ошибка при загрузке файла", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

class FileUploadResponse {
    private String url;

    public FileUploadResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
} 