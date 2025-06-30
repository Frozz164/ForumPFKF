package org.example.repository;

import org.example.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByUserId(Long userId);
    List<Donation> findByFundraisingId(Long fundraisingId);
    List<Donation> findByRecurringTrue();
    
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.fundraising.id = :fundraisingId AND d.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalAmountByFundraisingId(Long fundraisingId);
    
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.user.id = :userId AND d.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalAmountByUserId(Long userId);

    int countByUserId(Long userId);
    
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.user.id = ?1")
    double sumAmountByUserId(Long userId);
} 