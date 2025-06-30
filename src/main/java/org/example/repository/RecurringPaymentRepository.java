package org.example.repository;

import org.example.model.RecurringPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {
    List<RecurringPayment> findByUserId(Long userId);
    List<RecurringPayment> findByFundraisingId(Long fundraisingId);
    List<RecurringPayment> findByIsActiveTrue();
    List<RecurringPayment> findByIsActiveTrueAndNextPaymentDateBefore(LocalDateTime date);
    int countByUserIdAndIsActiveTrue(Long userId);
    List<RecurringPayment> findByUserIdAndIsActiveTrue(Long userId);
} 