package org.example.dto;

import lombok.Data;

@Data
public class DocumentResponse {
    private String fullImage;
    private String thumbnail;
    private String title;
    private String description;
    private String type;
} 