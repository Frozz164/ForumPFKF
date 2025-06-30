package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.RecurringPayment;
import org.example.model.User;
import org.example.model.Fundraising;
import org.example.repository.RecurringPaymentRepository;
import org.example.repository.FundraisingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecurringPaymentService {
    private final RecurringPaymentRepository recurringPaymentRepository;
    private final FundraisingRepository fundraisingRepository;

    public RecurringPaymentService(
            RecurringPaymentRepository recurringPaymentRepository,
            @Lazy FundraisingRepository fundraisingRepository) {
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.fundraisingRepository = fundraisingRepository;
    }

    @Transactional
    public RecurringPayment createRecurringPayment(User user, Long fundraisingId, BigDecimal amount, Integer paymentDay) {
        Fundraising fundraising = fundraisingRepository.findById(fundraisingId)
                .orElseThrow(() -> new RuntimeException("Fundraising not found"));

        RecurringPayment payment = new RecurringPayment();
        payment.setUser(user);
        payment.setFundraising(fundraising);
        payment.setAmount(amount);
        payment.setPaymentDay(paymentDay);
        payment.setNextPaymentDate(calculateNextPaymentDate(paymentDay));

        return recurringPaymentRepository.save(payment);
    }

    @Transactional
    public void cancelRecurringPayment(Long paymentId, Long userId) {
        RecurringPayment payment = recurringPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Recurring payment not found"));

        if (!payment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        payment.setActive(false);
        recurringPaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<RecurringPayment> getUserRecurringPayments(Long userId) {
        return recurringPaymentRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Transactional
    public void processRecurringPayments() {
        LocalDateTime now = LocalDateTime.now();
        List<RecurringPayment> paymentsToProcess = recurringPaymentRepository
                .findByIsActiveTrueAndNextPaymentDateBefore(now);

        for (RecurringPayment payment : paymentsToProcess) {
            processPayment(payment);
            updateNextPaymentDate(payment);
        }
    }

    private void processPayment(RecurringPayment payment) {
        Fundraising fundraising = payment.getFundraising();
        fundraising.setCurrentAmount(fundraising.getCurrentAmount().add(payment.getAmount()));
        fundraisingRepository.save(fundraising);
        // TODO: Add actual payment processing logic here
    }

    private void updateNextPaymentDate(RecurringPayment payment) {
        payment.setNextPaymentDate(calculateNextPaymentDate(payment.getPaymentDay()));
        recurringPaymentRepository.save(payment);
    }

    private LocalDateTime calculateNextPaymentDate(Integer paymentDay) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextPayment = now.withDayOfMonth(paymentDay);
        
        if (nextPayment.isBefore(now) || nextPayment.isEqual(now)) {
            nextPayment = nextPayment.plusMonths(1);
        }
        
        return nextPayment;
    }
} 