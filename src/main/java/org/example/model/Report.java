package org.example.model;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "reports")
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundraising_id", nullable = false)
    private Fundraising fundraising;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "spent_amount", nullable = false)
    private BigDecimal spentAmount;

    @ElementCollection
    @CollectionTable(name = "report_documents", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "document_url")
    private List<String> documentUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "report_documents", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "document_description")
    private List<String> documentDescriptions = new ArrayList<>();

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_verified")
    private boolean verified;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reportDate == null) {
            reportDate = LocalDateTime.now();
        }
        verified = false;
    }
} 