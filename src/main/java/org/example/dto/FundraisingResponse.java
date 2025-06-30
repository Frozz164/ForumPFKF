package org.example.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.model.User;

@Data
public class FundraisingResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private boolean active;
    private boolean completed;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String diagnosis;
    private List<DocumentDTO> documents;
    private String imageUrl;
    private User createdBy;
} 