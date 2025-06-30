package org.example.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Set;

@Data
public class CharityRequest {
    
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    private String websiteUrl;

    private Set<String> categories;

    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid registration number format")
    private String registrationNumber;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    private String contactPhone;
    private String contactAddress;
    
    // Банковские реквизиты
    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "INN is required")
    @Pattern(regexp = "^\\d{10}|\\d{12}$", message = "Invalid INN format")
    private String inn;

    @NotBlank(message = "KPP is required")
    @Pattern(regexp = "^\\d{9}$", message = "Invalid KPP format")
    private String kpp;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^\\d{20}$", message = "Invalid account number format")
    private String accountNumber;

    @NotBlank(message = "BIK is required")
    @Pattern(regexp = "^\\d{9}$", message = "Invalid BIK format")
    private String bik;

    @NotBlank(message = "Bank name is required")
    private String bankName;
    
    private boolean verified;

    // Поля для документов
    private List<MultipartFile> documents;
    private List<String> documentTitles;
    private List<String> documentDescriptions;
} 