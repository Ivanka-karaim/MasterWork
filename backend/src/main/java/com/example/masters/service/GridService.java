package com.example.masters.service;

import com.example.masters.dto.notification.NotificationRequest;
import com.example.masters.entity.*;
import com.example.masters.entity.enums.Role;
import com.example.masters.exception.BadRequestException;
import com.example.masters.mqtt.MqttService;
import com.example.masters.repository.ControlRepository;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.StatusRepository;
import com.example.masters.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class GridService {
    private final StatusRepository statusRepository;
    private final ControlRepository controlRepository;
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    public void gridSetting(String statusGrid, String deviceInventoryNumber) {
        Device device = deviceRepository.findByInventoryNumber(deviceInventoryNumber).orElse(null);
        if (device != null) {
            Control control = controlRepository.findFirstByDeviceIdOrderByDateTimeDesc(device.getId()).orElse(null);
            if (device.getMode()!=null && device.getMode().equals("AUTO")) {
                Status status = statusRepository.findFirstByDeviceInventoryNumberOrderByDateTimeDesc(deviceInventoryNumber).orElse(null);
                if (Objects.equals(statusGrid, "OFFLINE") && (status == null || !Objects.equals(status.getActionType(), "ON"))) {
                    boolean sendMessage = executeAction(deviceInventoryNumber, "ON");
                    if (!sendMessage) {
                        throw new BadRequestException("Уппссс, щось пішло не так");
                    } else {
                        sendMessageForAdmin(device.getTitle(), "ON");
                    }

                } else if (Objects.equals(statusGrid, "ONLINE") && (status == null || !Objects.equals(status.getActionType(), "OFF"))) {
                    boolean sendMessage = executeAction(deviceInventoryNumber, "OFF");
                    if (!sendMessage) {
                        throw new BadRequestException("Уппссс, щось пішло не так");
                    } else {
                        sendMessageForAdmin(device.getTitle(), "OFF");
                    }
                }


            }
        }

    }

    @Transactional
    public void gridConnection(String statusGrid, String deviceInventoryNumber) {
        Status status = statusRepository.findFirstByDeviceInventoryNumberOrderByDateTimeDesc(deviceInventoryNumber).orElse(null);
        if (status != null && !status.getActionType().equals(statusGrid)) {
            NotificationRequest request;
            if (Objects.equals(statusGrid, "OFFLINE")) {
                request = NotificationRequest.builder()
                        .title("Мережа зникла")
                        .message("Чомусь зникла мережа")
                        .build();
            } else {
                request = NotificationRequest.builder()
                        .title("Мережа з'явилась")
                        .message("Мережа вже з'явилась")
                        .build();
            }
            List<User> users = userRepository.findByRole(Role.ADMIN);
            for (User user : users) {
                notificationService.createNotification(user.getId(), request);
            }


        }
    }


    private boolean executeAction(String deviceInventoryNumber, String action) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String payload = mapper.writeValueAsString(Map.of(
                    "action", action,
                    "value", 0
            ));

            mqttService.sendCommand(deviceInventoryNumber, payload);


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void sendMessageForAdmin(String deviceTitle, String action) {
        NotificationRequest request;
        if (action.equals("ON")) {
            request = NotificationRequest.builder()
                    .title(deviceTitle + " увімкнено")
                    .message("Пристрій " + deviceTitle + (Objects.equals(action, "ON") ?
                            " було увімкнено" : " було вимкнено") + ", оскільки мережу втрачено ")
                    .build();
        } else {
            request = NotificationRequest.builder()
                    .title(deviceTitle + " вимкнено")
                    .message("Пристрій " + deviceTitle + (Objects.equals(action, "ON") ?
                            " було увімкнено" : " було вимкнено") + ", оскільки мережу з'явилась ")
                    .build();
        }

        List<User> users = userRepository.findByRole(Role.ADMIN);
        for (User user : users) {
            notificationService.createNotification(user.getId(), request);
        }
    }


}
