package com.example.masters.service;

import com.example.masters.dto.control.ControlResponse;
import com.example.masters.entity.Control;
import com.example.masters.entity.User;
import com.example.masters.repository.ControlRepository;
import com.example.masters.repository.StatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ControlService {

    private final ControlRepository controlRepository;

    @Transactional(readOnly = true)
    public List<ControlResponse> getAllActionsForUser(User user){
        List<Control> controls = controlRepository.findAllByUser(user);
        return controls.stream()
                .map(this::convertControlToResponse)
                .collect(Collectors.toList());

    }
    @Transactional(readOnly = true)
    public List<ControlResponse> getAllActionsForDeviceId(UUID deviceId, String fromDate, String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Якщо дати не передані — встановлюємо дефолтні межі
        LocalDateTime start = (fromDate != null && !fromDate.isBlank())
                ? LocalDate.parse(fromDate, formatter).atStartOfDay()
                : LocalDate.of(1970, 1, 1).atStartOfDay();

        LocalDateTime end = (toDate != null && !toDate.isBlank())
                ? LocalDate.parse(toDate, formatter).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        List<Control> controls = controlRepository.findAllByDeviceIdAndDateTimeBetweenOrderByDateTimeDesc(deviceId, Timestamp.valueOf(start), Timestamp.valueOf(end));

        return controls.stream()
                .map(this::convertControlToResponse)
                .collect(Collectors.toList());
    }


    private ControlResponse convertControlToResponse(Control control){
        return ControlResponse.builder()
                .id(control.getId())
                .actionType(control.getActionType())
                .actionValue(control.getActionValue())
                .dateTime(control.getDateTime().toLocalDateTime())
                .deviceName(control.getDevice().getTitle())
                .userName(control.getUser().getFullName())
                .build();
    }
}
