package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateReportRequest;
import org.example.model.Report;
import org.example.service.JwtService;
import org.example.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {
    private final ReportService reportService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<Report> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);
        log.info("Получен запрос на создание отчета от пользователя {}", userId);
        return ResponseEntity.ok(reportService.createReport(request, userId));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<Report> getReportById(@PathVariable Long reportId) {
        log.debug("Получен запрос на получение отчета {}", reportId);
        return ResponseEntity.ok(reportService.getReportById(reportId));
    }

    @GetMapping("/fundraising/{fundraisingId}")
    public ResponseEntity<List<Report>> getFundraisingReports(@PathVariable Long fundraisingId) {
        log.debug("Получен запрос на получение отчетов фандрайзинга {}", fundraisingId);
        return ResponseEntity.ok(reportService.getFundraisingReports(fundraisingId));
    }

    @GetMapping("/charity/{charityId}")
    public ResponseEntity<List<Report>> getCharityReports(@PathVariable Long charityId) {
        log.debug("Получен запрос на получение отчетов благотворительной организации {}", charityId);
        return ResponseEntity.ok(reportService.getCharityReports(charityId));
    }

    @PostMapping("/{reportId}/verify")
    public ResponseEntity<Report> verifyReport(@PathVariable Long reportId) {
        log.info("Получен запрос на верификацию отчета {}", reportId);
        return ResponseEntity.ok(reportService.verifyReport(reportId));
    }

    @PostMapping("/{reportId}/documents")
    public ResponseEntity<Report> uploadDocuments(
            @PathVariable Long reportId,
            @RequestParam("files") List<MultipartFile> documents,
            @RequestParam(value = "descriptions", required = false) List<String> descriptions) {
        log.info("Получен запрос на загрузку документов для отчета: {}", reportId);
        return ResponseEntity.ok(reportService.uploadDocuments(reportId, documents, descriptions));
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadReportFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Получен запрос на загрузку файла для отчета: {}", file.getOriginalFilename());
            String fileUrl = reportService.uploadFile(file);
            return ResponseEntity.ok(new FileUploadResponse(fileUrl));
        } catch (IOException e) {
            log.error("Ошибка при загрузке файла", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 