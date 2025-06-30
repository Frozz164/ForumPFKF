package org.example.repository;

import org.example.model.Fundraising;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundraisingRepository extends JpaRepository<Fundraising, Long> {
    List<Fundraising> findByCharityId(Long charityId);
    List<Fundraising> findByActiveTrue();
    List<Fundraising> findByCompletedTrue();
    List<Fundraising> findByActiveTrueAndCompletedFalse();
    Optional<Fundraising> findByCharityIdAndTargetAmount(Long charityId, BigDecimal targetAmount);
} 