package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateReportRequest;
import org.example.model.Fundraising;
import org.example.model.Report;
import org.example.repository.FundraisingRepository;
import org.example.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final FundraisingRepository fundraisingRepository;
    private final FundraisingService fundraisingService;

    public ReportService(
            ReportRepository reportRepository,
            @Lazy FundraisingRepository fundraisingRepository,
            @Lazy FundraisingService fundraisingService) {
        this.reportRepository = reportRepository;
        this.fundraisingRepository = fundraisingRepository;
        this.fundraisingService = fundraisingService;
    }

    @Transactional
    public Report createReport(CreateReportRequest request, Long userId) {
        log.info("Создание отчета для фандрайзинга {} пользователем {}", 
                request.getFundraisingId(), userId);

        if (!fundraisingService.isUserFundraisingCreator(userId, request.getFundraisingId())) {
            log.error("Пользователь {} не является создателем фандрайзинга {}", 
                    userId, request.getFundraisingId());
            throw new RuntimeException("У вас нет прав для создания отчета по этому сбору");
        }

        Fundraising fundraising = fundraisingRepository.findById(request.getFundraisingId())
                .orElseThrow(() -> {
                    log.error("Фандрайзинг с ID {} не найден", request.getFundraisingId());
                    return new RuntimeException("Фандрайзинг не найден");
                });

        // Проверяем, что сумма отчета соответствует собранной сумме
        BigDecimal formattedSpentAmount = request.getSpentAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal formattedCurrentAmount = fundraising.getCurrentAmount().setScale(2, RoundingMode.HALF_UP);
        
        if (!formattedSpentAmount.equals(formattedCurrentAmount)) {
            log.error("Сумма в отчете ({}) не соответствует собранной сумме ({})",
                    formattedSpentAmount, formattedCurrentAmount);
            throw new RuntimeException("Сумма в отчете должна соответствовать собранной сумме");
        }

        Report report = new Report();
        report.setFundraising(fundraising);
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setSpentAmount(request.getSpentAmount());
        report.setDocumentUrls(request.getDocumentUrls());
        report.setDocumentDescriptions(request.getDocumentDescriptions());
        report.setReportDate(request.getReportDate() != null ? request.getReportDate() : LocalDateTime.now());

        report = reportRepository.save(report);

        // После создания отчета, помечаем фандрайзинг как завершенный и неактивный
        fundraising.setCompleted(true);
        fundraising.setActive(false);
        fundraisingRepository.save(fundraising);

        log.info("Отчет успешно создан, ID: {}, фандрайзинг {} помечен как завершенный", 
                report.getId(), fundraising.getId());

        return report;
    }

    @Transactional(readOnly = true)
    public List<Report> getFundraisingReports(Long fundraisingId) {
        log.debug("Получение отчетов для фандрайзинга {}", fundraisingId);
        return reportRepository.findByFundraisingIdOrderByReportDateDesc(fundraisingId);
    }

    @Transactional(readOnly = true)
    public List<Report> getCharityReports(Long charityId) {
        log.debug("Получение отчетов для благотворительной организации {}", charityId);
        List<Fundraising> fundraisings = fundraisingRepository.findByCharityId(charityId);
        return fundraisings.stream()
                .map(f -> reportRepository.findByFundraisingIdOrderByReportDateDesc(f.getId()))
                .flatMap(List::stream)
                .sorted((r1, r2) -> r2.getReportDate().compareTo(r1.getReportDate()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Report verifyReport(Long reportId) {
        log.info("Верификация отчета {}", reportId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Отчет с ID {} не найден", reportId);
                    return new RuntimeException("Отчет не найден");
                });

        report.setVerified(true);
        return reportRepository.save(report);
    }

    @Transactional
    public Report uploadDocuments(Long reportId, List<MultipartFile> documents, List<String> descriptions) {
        log.info("Загрузка документов для отчета: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Отчет с ID {} не найден", reportId);
                    return new RuntimeException("Отчет не найден");
                });

        List<String> documentUrls = new ArrayList<>();

        try {
            for (int i = 0; i < documents.size(); i++) {
                MultipartFile document = documents.get(i);
                String description = (descriptions != null && descriptions.size() > i) ? descriptions.get(i) : "";

                // Генерируем уникальное имя файла
                String fileName = UUID.randomUUID().toString() + "_" + document.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Сохраняем файл
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Добавляем URL документа
                String documentUrl = "/uploads/" + fileName;
                documentUrls.add(documentUrl);

                log.info("Документ успешно загружен: {}", documentUrl);
            }

            // Обновляем список документов в отчете
            report.getDocumentUrls().addAll(documentUrls);
            if (descriptions != null) {
                report.getDocumentDescriptions().addAll(descriptions);
            }
            report = reportRepository.save(report);

            log.info("Документы успешно добавлены в отчет {}", reportId);
            return report;

        } catch (IOException e) {
            log.error("Ошибка при загрузке документов", e);
            throw new RuntimeException("Не удалось загрузить документы", e);
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Создаем директорию, если не существует
        File directory = new File("uploads");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Генерируем уникальное имя файла
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get("uploads").resolve(newFileName);

        // Сохраняем файл с использованием REPLACE_EXISTING
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Формируем URL для доступа к файлу
        String fileUrl = "/uploads/" + newFileName;

        log.info("Файл для отчета успешно загружен: {}", fileUrl);

        return fileUrl;
    }

    @Transactional(readOnly = true)
    public Report getReportById(Long reportId) {
        log.debug("Получение отчета по ID: {}", reportId);
        return reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("Отчет с ID {} не найден", reportId);
                    return new RuntimeException("Отчет не найден");
                });
    }
} 