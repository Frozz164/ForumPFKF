package org.example.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.FundraisingRequest;
import org.example.model.Fundraising;
import org.example.service.FundraisingService;
import org.example.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fundraisings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FundraisingController {

    private final FundraisingService fundraisingService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<Fundraising> createFundraising(
            @Valid @RequestBody FundraisingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);
        log.info("Создание новой фандрайзинговой кампании от пользователя {}", userId);
        return ResponseEntity.ok(fundraisingService.createFundraising(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<Fundraising>> getAllFundraisings() {
        log.debug("Получение списка всех фандрайзинговых кампаний");
        return ResponseEntity.ok(fundraisingService.getAllFundraisings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fundraising> getFundraisingById(@PathVariable Long id) {
        log.debug("Получение фандрайзинговой кампании по ID: {}", id);
        return ResponseEntity.ok(fundraisingService.getFundraisingById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Fundraising>> getActiveFundraisings() {
        log.debug("Получение списка активных фандрайзинговых кампаний");
        return ResponseEntity.ok(fundraisingService.getActiveFundraisings());
    }

    @GetMapping("/charity/{charityId}")
    public ResponseEntity<List<Fundraising>> getCharityFundraisings(@PathVariable Long charityId) {
        log.debug("Получение списка кампаний для благотворительной организации: {}", charityId);
        return ResponseEntity.ok(fundraisingService.getFundraisingsByCharity(charityId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fundraising> updateFundraising(
            @PathVariable Long id,
            @Valid @RequestBody FundraisingRequest request) {
        log.info("Обновление фандрайзинговой кампании: {}", id);
        return ResponseEntity.ok(fundraisingService.updateFundraising(id, request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeFundraising(@PathVariable Long id) {
        log.info("Завершение фандрайзинговой кампании: {}", id);
        fundraisingService.completeFundraising(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFundraising(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        validateToken(authHeader);
        log.info("Удаление фандрайзинговой кампании: {}", id);
        fundraisingService.deleteFundraising(id);
        return ResponseEntity.noContent().build();
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Отсутствует или неверный формат токена авторизации");
            throw new RuntimeException("Unauthorized");
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            log.error("Невалидный JWT токен");
            throw new RuntimeException("Invalid token");
        }
    }
} 