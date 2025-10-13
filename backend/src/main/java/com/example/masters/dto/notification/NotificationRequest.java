package com.example.masters.dto.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
    private String title;
    private String message;
}
