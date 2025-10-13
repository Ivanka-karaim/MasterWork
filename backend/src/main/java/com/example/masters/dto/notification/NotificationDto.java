package com.example.masters.dto.notification;

import com.example.masters.dto.device.DeviceResponse;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class NotificationDto {
    private UUID id;
    private String title;
    private String message;
    private Timestamp dateTime;
    private boolean read;
    private DeviceResponse device;
}
