package org.example.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "fundraisings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Fundraising {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "charity_id", nullable = false)
    private Charity charity;

    @JsonIgnoreProperties("fundraising")
    @OneToMany(mappedBy = "fundraising", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Donation> donations = new ArrayList<>();

    @JsonIgnoreProperties("fundraising")
    @OneToMany(mappedBy = "fundraising", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurringPayment> recurringPayments = new ArrayList<>();

    @JsonIgnoreProperties("fundraising")
    @OneToMany(mappedBy = "fundraising", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false)
    private BigDecimal currentAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "is_completed")
    private boolean completed;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @ElementCollection
    @CollectionTable(name = "fundraising_documents", joinColumns = @JoinColumn(name = "fundraising_id"))
    private List<Document> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        currentAmount = BigDecimal.ZERO;
        active = true;
        completed = false;
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
    }
} 