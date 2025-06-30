package org.example.repository;

import org.example.model.Charity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CharityRepository extends JpaRepository<Charity, Long> {
    boolean existsByRegistrationNumber(String registrationNumber);
    
    @Query("SELECT c FROM Charity c JOIN c.categories cat WHERE cat = :category")
    List<Charity> findByCategory(@Param("category") String category);
    
    List<Charity> findByVerifiedTrue();
    List<Charity> findByActiveTrue();
} 