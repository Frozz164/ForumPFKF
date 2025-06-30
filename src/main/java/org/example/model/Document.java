package org.example.model;

import javax.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Document {
    private String url;
    private String title;
    private String description;
} 