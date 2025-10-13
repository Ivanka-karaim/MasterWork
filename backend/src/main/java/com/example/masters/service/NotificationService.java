package com.example.masters.service;

import com.example.masters.dto.notification.NotificationDto;
import com.example.masters.dto.notification.NotificationRequest;
import com.example.masters.entity.Notification;
import com.example.masters.entity.User;
import com.example.masters.repository.NotificationRepository;
import com.example.masters.repository.UserRepository;
import com.example.masters.security.GlobalResolver;
import com.example.masters.websocket.NotificationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationHandler websocketHandler;
    private final GlobalResolver globalResolver;
    private final UserRepository userRepository;
    private final DeviceService deviceService;

    public void createNotification(UUID userId, NotificationRequest notificationRequest) {
        Notification notification = Notification.builder()
                .user(userRepository.findById(userId).orElse(null))
                .title(notificationRequest.getTitle())
                .message(notificationRequest.getMessage())
                .isRead(false)
                .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        notificationRepository.save(notification);
        notificationRepository.flush();
        websocketHandler.sendToUser(notification);
    }

    public List<NotificationDto> getUserNotifications() {
        User user = globalResolver.requireCurrentUser();
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByDateTimeDesc(user.getId());
        return notifications.stream()
                .map(this::convertNotificationToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationDto convertNotificationToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .dateTime(notification.getDateTime())
                .device(notification.getDevice() != null? deviceService.convertToDeviceResponse(notification.getDevice()): null)
                .build();
    }
}

