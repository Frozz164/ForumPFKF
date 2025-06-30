package org.example.dto;

import lombok.Data;
import org.example.model.User;
import org.example.dto.DocumentDTO;
import org.example.dto.FundraisingResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class CharityResponse {
    private Long id;
    private String name;
    private String description;
    private String websiteUrl;
    private Set<String> categories;
    private String registrationNumber;
    private String contactEmail;
    private String contactPhone;
    private String contactAddress;
    private String organizationName;
    private String inn;
    private String kpp;
    private String accountNumber;
    private String bik;
    private String bankName;
    private boolean verified;
    private boolean active;
    private LocalDateTime createdAt;
    private User createdBy;
    private List<DocumentDTO> documents;
    private List<FundraisingResponse> fundraisings;
    private BigDecimal totalDonations;
    
    // Статистика
    private Long totalDonors;
    private Integer recurringDonationsCount;
    private Integer completedFundraisingsCount;
} 