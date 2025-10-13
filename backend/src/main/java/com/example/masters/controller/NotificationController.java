package com.example.masters.controller;

import com.example.masters.dto.ApiResponse;
import com.example.masters.dto.notification.NotificationDto;
import com.example.masters.dto.notification.NotificationRequest;
import com.example.masters.entity.Notification;
import com.example.masters.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications() {
        List<NotificationDto> notifications = service.getUserNotifications();
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", notifications));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> create(@PathVariable UUID userId, @RequestBody NotificationRequest notificationRequest) {
        service.createNotification(userId, notificationRequest);
        return ResponseEntity.ok("Notification created");
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markRead(@PathVariable UUID id) {
        service.markAsRead(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", null));
    }
}

