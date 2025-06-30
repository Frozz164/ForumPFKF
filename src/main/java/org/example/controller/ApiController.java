package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AuthResponse;
import org.example.dto.CharityRequest;
import org.example.dto.CharityResponse;
import org.example.dto.DonationRequest;
import org.example.dto.ErrorResponse;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.model.Charity;
import org.example.model.Donation;
import org.example.model.Document;
import org.example.service.CharityService;
import org.example.service.DonationService;
import org.example.service.JwtService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ApiController {

    private final UserService userService;
    private final CharityService charityService;
    private final DonationService donationService;
    private final JwtService jwtService;

    // Аутентификация
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Получен запрос на регистрацию: {}", request.getEmail());
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Получен запрос на вход: {}", request.getEmail());
        return ResponseEntity.ok(userService.login(request));
    }

    // Благотворительные организации
    @GetMapping("/charities")
    public ResponseEntity<List<CharityResponse>> getAllCharities() {
        log.debug("Получен запрос на список благотворительных организаций");
        return ResponseEntity.ok(charityService.getAllCharities());
    }

    @GetMapping("/charities/{id}")
    public ResponseEntity<CharityResponse> getCharityById(@PathVariable Long id) {
        log.debug("Получен запрос на получение благотворительной организации: {}", id);
        return ResponseEntity.ok(charityService.getCharityById(id));
    }

    @PostMapping("/charities")
    public ResponseEntity<CharityResponse> createCharity(
            @ModelAttribute CharityRequest request,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents,
            @RequestParam(value = "titles[]", required = false) List<String> titles,
            @RequestParam(value = "descriptions[]", required = false) List<String> descriptions,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = validateTokenAndGetUserId(authHeader);
        log.info("Получен запрос на создание благотворительной организации");
        log.info("Категории в запросе: {}", request.getCategories());
        
        // Генерируем регистрационный номер, если он не указан
        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().trim().isEmpty()) {
            request.setRegistrationNumber("ORG-" + System.currentTimeMillis());
        }
        
        // Устанавливаем логотип
        
        // Устанавливаем документы и их метаданные
        request.setDocuments(documents);
        request.setDocumentTitles(titles);
        request.setDocumentDescriptions(descriptions);
        
        log.info("Регистрационный номер: {}", request.getRegistrationNumber());
        log.info("Логотип: {}", logo != null ? logo.getOriginalFilename() : "не загружен");
        log.info("Количество документов: {}", documents != null ? documents.size() : 0);
        log.info("Количество заголовков: {}", titles != null ? titles.size() : 0);
        log.info("Количество описаний: {}", descriptions != null ? descriptions.size() : 0);
        
        log.info("Банковские реквизиты:");
        log.info("organizationName: {}", request.getOrganizationName());
        log.info("inn: {}", request.getInn());
        log.info("kpp: {}", request.getKpp());
        log.info("accountNumber: {}", request.getAccountNumber());
        log.info("bik: {}", request.getBik());
        log.info("bankName: {}", request.getBankName());
        
        return ResponseEntity.ok(charityService.createCharity(request, userId));
    }

    @PutMapping("/charities/{id}")
    public ResponseEntity<CharityResponse> updateCharity(
            @PathVariable Long id,
            @ModelAttribute CharityRequest request,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = validateTokenAndGetUserId(authHeader);
        log.info("Получен запрос на обновление благотворительной организации: {}", id);
        return ResponseEntity.ok(charityService.updateCharity(id, request, userId));
    }

    @PutMapping("/charities/{id}/verify")
    public ResponseEntity<CharityResponse> verifyCharity(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        validateTokenAndGetUserId(authHeader);
        log.info("Получен запрос на верификацию благотворительной организации: {}", id);
        return ResponseEntity.ok(charityService.verifyCharity(id));
    }

    @PostMapping("/charities/documents")
    public ResponseEntity<CharityResponse> uploadCharityDocuments(
            @RequestParam("charityId") Long charityId,
            @RequestParam("documents") List<MultipartFile> documents,
            @RequestParam(value = "titles[]", required = false) List<String> titles,
            @RequestParam(value = "descriptions[]", required = false) List<String> descriptions,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = validateTokenAndGetUserId(authHeader);
        log.info("Получен запрос на загрузку документов для фонда: {}", charityId);
        return ResponseEntity.ok(charityService.uploadDocuments(charityId, documents, titles, descriptions, userId));
    }

    // Пожертвования
    @PostMapping("/donations")
    public ResponseEntity<Donation> createDonation(
            @RequestBody DonationRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = validateTokenAndGetUserId(authHeader);
        log.info("Получен запрос на создание пожертвования от пользователя: {}", userId);
        return ResponseEntity.ok(donationService.createDonation(request, userId));
    }

    @GetMapping("/donations/user")
    public ResponseEntity<List<Donation>> getUserDonations(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = validateTokenAndGetUserId(authHeader);
        log.debug("Получен запрос на список пожертвований пользователя: {}", userId);
        return ResponseEntity.ok(donationService.getUserDonations(userId));
    }
    
    @DeleteMapping("/charities/{id}")
    public ResponseEntity<Void> deleteCharity(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        validateTokenAndGetUserId(authHeader);
        log.info("Получен запрос на удаление благотворительной организации: {}", id);
        charityService.deleteCharity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/donations/fundraising/{fundraisingId}")
    public ResponseEntity<List<Donation>> getFundraisingDonations(@PathVariable Long fundraisingId) {
        log.debug("Получен запрос на список пожертвований для кампании: {}", fundraisingId);
        return ResponseEntity.ok(donationService.getFundraisingDonations(fundraisingId));
    }

    @GetMapping("/auth/check-role")
    public ResponseEntity<?> checkRole(@RequestHeader("Authorization") String authHeader) {
        Long userId = validateTokenAndGetUserId(authHeader);
        boolean isAdmin = userService.isUserAdmin(userId);
        return ResponseEntity.ok(Map.of("role", isAdmin ? "ADMIN" : "USER"));
    }

    // Обработка ошибок
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Ошибка при обработке запроса: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    // Вспомогательный метод для валидации токена
    private Long validateTokenAndGetUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Отсутствует или неверный формат токена авторизации");
            throw new RuntimeException("Unauthorized");
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            log.error("Невалидный JWT токен");
            throw new RuntimeException("Invalid token");
        }

        return jwtService.extractUserId(token);
    }
} 