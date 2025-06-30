package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.DonationRequest;
import org.example.model.Donation;
import org.example.model.Fundraising;
import org.example.model.User;
import org.example.repository.DonationRepository;
import org.example.repository.FundraisingRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final FundraisingRepository fundraisingRepository;
    private final RecurringPaymentService recurringPaymentService;

    public DonationService(
            DonationRepository donationRepository,
            UserRepository userRepository,
            FundraisingRepository fundraisingRepository,
            @Lazy RecurringPaymentService recurringPaymentService) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.fundraisingRepository = fundraisingRepository;
        this.recurringPaymentService = recurringPaymentService;
    }

    @Transactional
    public Donation createDonation(DonationRequest request, Long userId) {
        log.info("Создание нового пожертвования от пользователя {} для кампании {}", 
                userId, request.getFundraisingId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Невозможно создать пожертвование: пользователь с ID {} не найден", userId);
                    return new RuntimeException("Пользователь не найден");
                });

        // Если fundraisingId не указан, ищем основной фонд
        Fundraising fundraising;
        if (request.getFundraisingId() == null) {
            // Получаем список всех фандрайзингов и ищем основной
            List<Fundraising> fundraisings = fundraisingRepository.findByCharityId(request.getCharityId());
            fundraising = fundraisings.stream()
                    .filter(f -> f.isActive() && f.getTargetAmount().compareTo(new BigDecimal("999999999999")) >= 0)
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("Не найден основной фонд для charity {}", request.getCharityId());
                        return new RuntimeException("Основной фонд не найден");
                    });
            log.info("Найден основной фонд: {} ({})", fundraising.getId(), fundraising.getCharity().getName());
        } else {
            fundraising = fundraisingRepository.findById(request.getFundraisingId())
                    .orElseThrow(() -> {
                        log.error("Невозможно создать пожертвование: кампания с ID {} не найдена", 
                                request.getFundraisingId());
                        return new RuntimeException("Фандрайзинговая кампания не найдена");
                    });
        }

        if (!fundraising.isActive()) {
            log.warn("Попытка пожертвования в неактивную кампанию: {}", fundraising.getId());
            throw new RuntimeException("Кампания больше не принимает пожертвования");
        }

        // Проверяем, верифицирован ли фонд
        if (!fundraising.getCharity().isVerified()) {
            log.warn("Попытка пожертвования в неверифицированный фонд: {}", fundraising.getCharity().getId());
            throw new RuntimeException("Фонд должен быть верифицирован для приема пожертвований");
        }

        // Для основного фонда не проверяем лимит целевой суммы
        if (fundraising.getTargetAmount().compareTo(new BigDecimal("999999999999")) < 0) {
            BigDecimal currentAmount = fundraising.getCurrentAmount();
            BigDecimal targetAmount = fundraising.getTargetAmount();
            BigDecimal remainingAmount = targetAmount.subtract(currentAmount);
            
            if (request.getAmount().compareTo(remainingAmount) > 0) {
                log.warn("Попытка пожертвования суммы больше необходимой. Запрошено: {}, Осталось собрать: {}", 
                        request.getAmount(), remainingAmount);
                throw new RuntimeException(String.format(
                    "Сумма пожертвования превышает оставшуюся необходимую сумму. Максимально возможная сумма: %s ₽", 
                    remainingAmount.toString()));
            }
        }

        Donation donation = new Donation();
        donation.setUser(user);
        donation.setFundraising(fundraising);
        donation.setAmount(request.getAmount());
        donation.setMessage(request.getMessage());
        donation.setAnonymous(request.isAnonymous());
        donation.setCreatedAt(LocalDateTime.now());
        donation.setPaymentStatus(Donation.PaymentStatus.COMPLETED);
        donation.setPaymentMethod(request.getPaymentMethod());
        donation.setRecurring(request.isRecurring());
        donation.setRecurringInterval(request.getRecurringInterval());

        donation = donationRepository.save(donation);
        
        fundraising.setCurrentAmount(fundraising.getCurrentAmount().add(request.getAmount()));
        fundraisingRepository.save(fundraising);
        
        if (request.isRecurring()) {
            try {
                int paymentDay = LocalDateTime.now().getDayOfMonth();
                recurringPaymentService.createRecurringPayment(user, fundraising.getId(), request.getAmount(), paymentDay);
                log.info("Создан регулярный платеж для пожертвования {} ({})", 
                        donation.getId(), fundraising.getCharity().getName());
            } catch (Exception e) {
                log.error("Ошибка при создании регулярного платежа для пожертвования {}: {}", 
                        donation.getId(), e.getMessage());
            }
        }
        
        BigDecimal totalAmount = donationRepository.getTotalAmountByFundraisingId(fundraising.getId());
        log.info("Пожертвование успешно создано. ID: {}, Сумма: {}, Общая сумма кампании: {}, Фонд: {}", 
                donation.getId(), donation.getAmount(), totalAmount, fundraising.getCharity().getName());

        // Проверяем завершение только для обычных фандрайзингов
        if (fundraising.getTargetAmount().compareTo(new BigDecimal("999999999999")) < 0) {
            if (totalAmount != null && totalAmount.compareTo(fundraising.getTargetAmount()) >= 0) {
                log.info("Кампания {} достигла целевой суммы! ({} из {})", 
                        fundraising.getId(), totalAmount, fundraising.getTargetAmount());
                fundraising.setCompleted(true);
                fundraisingRepository.save(fundraising);
            }
        }

        return donation;
    }

    @Transactional(readOnly = true)
    public List<Donation> getUserDonations(Long userId) {
        log.debug("Получение списка пожертвований пользователя с ID: {}", userId);
        List<Donation> donations = donationRepository.findByUserId(userId);
        // Преобразуем paymentStatus в status для фронтенда и добавляем названия
        donations.forEach(donation -> {
            if (donation.getPaymentStatus() != null) {
                donation.setStatus(donation.getPaymentStatus().name());
            }
            if (donation.getFundraising() != null) {
                donation.setFundraisingTitle(donation.getFundraising().getTitle());
                if (donation.getFundraising().getCharity() != null) {
                    donation.setCharityName(donation.getFundraising().getCharity().getName());
                }
            }
        });
        return donations;
    }

    @Transactional(readOnly = true)
    public List<Donation> getFundraisingDonations(Long fundraisingId) {
        log.debug("Получение списка пожертвований для кампании с ID: {}", fundraisingId);
        List<Donation> donations = donationRepository.findByFundraisingId(fundraisingId);
        // Преобразуем paymentStatus в status для фронтенда и добавляем названия
        donations.forEach(donation -> {
            if (donation.getPaymentStatus() != null) {
                donation.setStatus(donation.getPaymentStatus().name());
            }
            if (donation.getFundraising() != null) {
                donation.setFundraisingTitle(donation.getFundraising().getTitle());
                if (donation.getFundraising().getCharity() != null) {
                    donation.setCharityName(donation.getFundraising().getCharity().getName());
                }
            }
        });
        return donations;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDonationAmount(Long fundraisingId) {
        log.debug("Подсчет общей суммы пожертвований для кампании с ID: {}", fundraisingId);
        return donationRepository.getTotalAmountByFundraisingId(fundraisingId);
    }

    @Transactional
    public void deleteDonation(Long id, Long userId) {
        log.info("Запрос на удаление пожертвования с ID: {} от пользователя {}", id, userId);
        
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Невозможно удалить: пожертвование с ID {} не найдено", id);
                    return new RuntimeException("Пожертвование не найдено");
                });

        if (!donation.getUser().getId().equals(userId)) {
            log.error("Попытка несанкционированного удаления пожертвования: пользователь {} пытается удалить пожертвование {}", 
                    userId, id);
            throw new RuntimeException("Нет прав на удаление этого пожертвования");
        }

        donationRepository.deleteById(id);
        log.info("Пожертвование успешно удалено: {}", id);
    }

    @Transactional
    public void updateDonationStatus(Long donationId, Donation.PaymentStatus status) {
        log.info("Updating donation status: {} -> {}", donationId, status);
        
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> {
                    log.error("Donation not found with id: {}", donationId);
                    return new RuntimeException("Donation not found");
                });
        
        if (donation.getPaymentStatus() == Donation.PaymentStatus.COMPLETED) {
            log.warn("Attempt to update completed donation: {}", donationId);
            throw new RuntimeException("Cannot update completed donation");
        }

        donation.setPaymentStatus(status);
        donationRepository.save(donation);
        log.info("Donation status updated successfully: {} -> {}", donationId, status);

        if (status == Donation.PaymentStatus.COMPLETED) {
            checkFundraisingCompletion(donation.getFundraising());
        }
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
    
    private void updateFundraisingAmount(Fundraising fundraising, BigDecimal amount) {
        log.debug("Updating fundraising amount: {} + {}", fundraising.getId(), amount);
        fundraising.setCurrentAmount(fundraising.getCurrentAmount().add(amount));
        fundraisingRepository.save(fundraising);
        log.info("Fundraising amount updated. New amount: {}", fundraising.getCurrentAmount());
    }

    private void checkFundraisingCompletion(Fundraising fundraising) {
        if (fundraising.getCurrentAmount().compareTo(fundraising.getTargetAmount()) >= 0) {
            log.info("Fundraising {} reached its target amount. Marking as completed.", fundraising.getId());
            fundraising.setCompleted(true);
            fundraising.setActive(false);
            fundraisingRepository.save(fundraising);
        }
    }
} 