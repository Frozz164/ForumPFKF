package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "donations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Donation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fundraising_id", nullable = false)
    private Fundraising fundraising;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column
    private String message;

    @Column(name = "is_anonymous")
    private boolean anonymous;

    @Column(name = "is_recurring")
    private boolean recurring;

    @Column(name = "recurring_interval")
    private String recurringInterval;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Поле для передачи статуса на фронтенд
    @Transient
    private String status;

    @Transient
    private String fundraisingTitle;

    @Transient
    private String charityName;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
    }

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
} 