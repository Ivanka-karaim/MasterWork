package com.example.masters.dto.management;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ActionRequest {
    @NotNull(message = "Device Id is required")
    private UUID deviceId;

    @NotBlank(message = "Action is required")
    private String action;

    private float value;
}
