package com.example.masters.websocket;

import com.example.masters.dto.notification.NotificationDto;
import com.example.masters.entity.Notification;
import com.example.masters.entity.User;
import com.example.masters.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component

public class NotificationHandler extends TextWebSocketHandler {

    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private NotificationRepository notificationRepository;

    public NotificationHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID userId = extractUserId(session); // витягнути з query ?userId=
        userSessions.put(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userSessions.values().remove(session);
    }

    public void sendToUser(NotificationDto notification, UUID userId){

        // Відправляємо через WebSocket (якщо онлайн)
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            String payload = null;
            try {
                payload = new ObjectMapper().writeValueAsString(notification);
                session.sendMessage(new TextMessage(payload));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private UUID extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery(); // ?userId=...
        String userIdParam = query.replace("userId=", "");
        return UUID.fromString(userIdParam);
    }
}
