package org.example.repository;

import org.example.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByFundraisingIdOrderByReportDateDesc(Long fundraisingId);
    boolean existsByFundraisingId(Long fundraisingId);
} 