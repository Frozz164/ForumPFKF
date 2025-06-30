package org.example.model;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "charities")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Charity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String websiteUrl;

    @ElementCollection
    @CollectionTable(name = "charity_categories", joinColumns = @JoinColumn(name = "charity_id"))
    @Column(name = "category")
    private Set<String> categories = new HashSet<>();

    @Column(name = "registrationnumber", unique = true, nullable = false)
    private String registrationNumber;

    private String contactEmail;
    private String contactPhone;
    private String contactAddress;

    // Банковские реквизиты
    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "inn", nullable = false)
    private String inn;

    @Column(name = "kpp", nullable = false)
    private String kpp;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "bik", nullable = false)
    private String bik;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @JsonManagedReference
    @OneToMany(mappedBy = "charity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fundraising> fundraisings = new ArrayList<>();

    private LocalDateTime createdAt;
    private boolean verified;
    private boolean active = true;

    @ElementCollection
    @CollectionTable(name = "charity_documents", joinColumns = @JoinColumn(name = "charity_id"))
    private List<Document> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        verified = false;
    }
} 