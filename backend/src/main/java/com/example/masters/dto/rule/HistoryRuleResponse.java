package com.example.masters.dto.rule;

import com.example.masters.dto.user.UserProfileResponse;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
public class HistoryRuleResponse {
    private LocalDateTime dateTime;
    private UserProfileResponse user;
}
