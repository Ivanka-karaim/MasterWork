package com.example.masters.dto.rule;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RuleRequest {
    private String ruleType;        // "SENSOR" або "TIME"

    private UUID deviceId;        // сенсор
    private String operator;        // ">", "<", "="
    private Double threshold;

    private UUID actionDeviceId;    // пристрій для керування
    private String actionType;      // "on", "off"
    private Double actionValue;       // 24

    private LocalDateTime triggerTime;  // для TIME правил
}

