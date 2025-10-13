package com.example.masters.dto.device;

import com.example.masters.entity.enums.Type;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DeviceResponse {
    private UUID id;
    private String title;
    private String description;
    private String inventoryNumber;
    private String image;
    private String type;
    private boolean isOn;
}
