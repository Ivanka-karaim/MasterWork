package com.example.masters.dto.device;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DeviceSimpleResponse {
    private UUID id;
    private String title;
    private String description;
    private String inventoryNumber;
    private String type;
}
