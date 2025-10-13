package com.example.masters.dto.control;

import com.example.masters.entity.Device;
import com.example.masters.entity.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ControlResponse {
    private UUID id;
    private LocalDateTime dateTime;
    private float actionValue;
    private String actionType;
    private String deviceName;
    private String userName;

}
