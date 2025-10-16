package com.example.masters.dto.rule;

import com.example.masters.dto.device.DeviceSimpleResponse;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RuleResponse {
    private UUID id;
    private String ruleType;
    private DeviceSimpleResponse sensor;
    private String operator;
    private Double threshold;
    private DeviceSimpleResponse actionDevice;
    private String action;
    private Double actionValue;
    private LocalDateTime triggerTime;
    private boolean active;
    private boolean strict;
    private List<HistoryRuleResponse> historyRules;
}
