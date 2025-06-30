package org.example.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateReportRequest {
    @NotNull(message = "ID фандрайзинга обязателен")
    private Long fundraisingId;

    @NotBlank(message = "Название отчета обязательно")
    private String title;

    @NotBlank(message = "Описание отчета обязательно")
    private String description;

    @NotNull(message = "Сумма расходов обязательна")
    @DecimalMin(value = "0.01", message = "Сумма расходов должна быть больше 0")
    private BigDecimal spentAmount;

    private List<String> documentUrls;
    private List<String> documentDescriptions;
    private LocalDateTime reportDate;
} 