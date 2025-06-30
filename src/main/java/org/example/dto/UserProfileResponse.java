package org.example.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean emailVerified;
    private int totalDonations;
    private double totalDonationAmount;
    private int activeRecurringPayments;
} 