package org.example.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DonationRequest {
    
    @NotNull(message = "Charity ID is required")
    private Long charityId; // ID благотворительного фонда

    private Long fundraisingId; // Может быть null для общего фонда

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String message;
    private boolean anonymous;
    private boolean recurring;
    private String recurringInterval;
    private String paymentMethod = "CARD"; // По умолчанию используем карту
} 