package com.example.masters.dto.management;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MeasurementDto {
    private UUID id;
    private Timestamp dateTime;
    private double value;
    private String parameterName;
    private String deviceInventoryNumber;
}
