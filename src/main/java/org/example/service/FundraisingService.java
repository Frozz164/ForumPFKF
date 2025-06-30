package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.FundraisingRequest;
import org.example.model.Charity;
import org.example.model.Fundraising;
import org.example.model.User;
import org.example.repository.CharityRepository;
import org.example.repository.DonationRepository;
import org.example.repository.FundraisingRepository;
import org.example.repository.ReportRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FundraisingService {

    private final FundraisingRepository fundraisingRepository;
    private final CharityRepository charityRepository;
    private final DonationRepository donationRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public FundraisingService(
            FundraisingRepository fundraisingRepository,
            CharityRepository charityRepository,
            DonationRepository donationRepository,
            @Lazy ReportRepository reportRepository,
            UserRepository userRepository) {
        this.fundraisingRepository = fundraisingRepository;
        this.charityRepository = charityRepository;
        this.donationRepository = donationRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Fundraising createFundraising(FundraisingRequest request, Long userId) {
        log.info("Создание новой фандрайзинговой кампании для организации {} пользователем {}", 
                request.getCharityId(), userId);

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new RuntimeException("Пользователь не найден");
                });

        Charity charity = charityRepository.findById(request.getCharityId())
                .orElseThrow(() -> {
                    log.error("Невозможно создать кампанию: организация с ID {} не найдена", 
                            request.getCharityId());
                    return new RuntimeException("Благотворительная организация не найдена");
                });

        if (!charity.isVerified()) {
            log.warn("Попытка создания кампании для неверифицированной организации: {}", charity.getId());
            throw new RuntimeException("Организация должна быть верифицирована для создания кампаний");
        }

        Fundraising fundraising = new Fundraising();
        fundraising.setCharity(charity);
        fundraising.setCreatedBy(creator);
        fundraising.setTitle(request.getTitle());
        fundraising.setDescription(request.getDescription());
        fundraising.setTargetAmount(request.getTargetAmount());
        fundraising.setStartDate(request.getStartDate());
        fundraising.setEndDate(request.getEndDate());
        fundraising.setImageUrl(request.getImageUrl());
        fundraising.setActive(true);
        fundraising.setCreatedAt(LocalDateTime.now());

        fundraising = fundraisingRepository.save(fundraising);
        log.info("Фандрайзинговая кампания успешно создана, ID: {}", fundraising.getId());

        return fundraising;
    }

    @Transactional(readOnly = true)
    public List<Fundraising> getAllFundraisings() {
        log.debug("Получение списка всех фандрайзинговых кампаний");
        return fundraisingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Fundraising> getActiveFundraisings() {
        log.debug("Получение списка активных фандрайзинговых кампаний");
        return fundraisingRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Fundraising getFundraisingById(Long id) {
        log.debug("Поиск фандрайзинговой кампании по ID: {}", id);
        return fundraisingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Фандрайзинговая кампания с ID {} не найдена", id);
                    return new RuntimeException("Фандрайзинговая кампания не найдена");
                });
    }

    @Transactional(readOnly = true)
    public List<Fundraising> getFundraisingsByCharity(Long charityId) {
        log.debug("Получение списка кампаний для организации с ID: {}", charityId);
        return fundraisingRepository.findByCharityId(charityId);
    }

    @Transactional
    public Fundraising updateFundraising(Long id, FundraisingRequest request) {
        log.info("Обновление данных фандрайзинговой кампании с ID: {}", id);

        Fundraising fundraising = fundraisingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Невозможно обновить: кампания с ID {} не найдена", id);
                    return new RuntimeException("Фандрайзинговая кампания не найдена");
                });

        if (!fundraising.getCharity().getId().equals(request.getCharityId())) {
            log.error("Попытка изменить организацию для кампании: {} -> {}", 
                    fundraising.getCharity().getId(), request.getCharityId());
            throw new RuntimeException("Нельзя изменить организацию для существующей кампании");
        }

        fundraising.setTitle(request.getTitle());
        fundraising.setDescription(request.getDescription());
        fundraising.setTargetAmount(request.getTargetAmount());
        fundraising.setStartDate(request.getStartDate());
        fundraising.setEndDate(request.getEndDate());
        fundraising.setImageUrl(request.getImageUrl());

        fundraising = fundraisingRepository.save(fundraising);
        log.info("Данные фандрайзинговой кампании успешно обновлены: {}", fundraising.getId());

        return fundraising;
    }

    @Transactional
    public void completeFundraising(Long id) {
        log.info("Завершение фандрайзинговой кампании с ID: {}", id);

        Fundraising fundraising = fundraisingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Невозможно завершить: кампания с ID {} не найдена", id);
                    return new RuntimeException("Фандрайзинговая кампания не найдена");
                });

        if (!fundraising.isActive()) {
            log.warn("Попытка завершить уже неактивную кампанию: {}", id);
            throw new RuntimeException("Кампания уже завершена");
        }

        if (!reportRepository.existsByFundraisingId(id)) {
            log.error("Невозможно завершить кампанию без отчета: {}", id);
            throw new RuntimeException("Невозможно завершить кампанию без отчета");
        }

        BigDecimal totalAmount = donationRepository.getTotalAmountByFundraisingId(id);
        fundraising.setActive(false);
        fundraising.setCompleted(true);
        
        fundraisingRepository.save(fundraising);
        log.info("Фандрайзинговая кампания {} успешно завершена. Собрано: {} из {}", 
                id, totalAmount, fundraising.getTargetAmount());
    }

    @Transactional
    public void deleteFundraising(Long id) {
        log.info("Запрос на удаление фандрайзинговой кампании с ID: {}", id);

        if (!fundraisingRepository.existsById(id)) {
            log.error("Невозможно удалить: кампания с ID {} не найдена", id);
            throw new RuntimeException("Фандрайзинговая кампания не найдена");
        }

        fundraisingRepository.deleteById(id);
        log.info("Фандрайзинговая кампания успешно удалена: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean isUserFundraisingCreator(Long userId, Long fundraisingId) {
        return fundraisingRepository.findById(fundraisingId)
                .map(fundraising -> fundraising.getCreatedBy().getId().equals(userId))
                .orElse(false);
    }
} 