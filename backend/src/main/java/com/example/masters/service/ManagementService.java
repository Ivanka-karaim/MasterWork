package com.example.masters.service;

import com.example.masters.dto.management.ActionRequest;
import com.example.masters.entity.Control;
import com.example.masters.entity.Device;
import com.example.masters.entity.Status;
import com.example.masters.entity.User;
import com.example.masters.exception.NotFoundException;
import com.example.masters.mqtt.MqttService;
import com.example.masters.repository.ControlRepository;
import com.example.masters.repository.StatusRepository;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.UserRepository;
import com.example.masters.security.GlobalResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@AllArgsConstructor
public class ManagementService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;
    private final ControlRepository controlRepository;
    private final GlobalResolver globalResolver;
    private final StatusRepository statusRepository;

    @Transactional(noRollbackFor = NotFoundException.class)
    public boolean createAction(ActionRequest actionRequest) {
        Device device = deviceRepository.findById(actionRequest.getDeviceId()).orElseThrow(()->new NotFoundException("Device not found"));
        User user = globalResolver.requireCurrentUser();
        try {

            ObjectMapper mapper = new ObjectMapper();
            String payload = mapper.writeValueAsString(Map.of(
                    "action", actionRequest.getAction(),
                    "value", actionRequest.getValue()
            ));

            // send to device
            mqttService.sendCommand(device.getInventoryNumber(), payload);

            // запис в бд про здійснену дію
            Control control = Control.builder()
                    .actionType(actionRequest.getAction())
                    .actionValue(actionRequest.getValue())
                    .user(user)
                    .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                    .device(device)
                    .build();
            Status status = Status.builder()
                    .actionType(actionRequest.getAction())
                    .actionValue(actionRequest.getValue())
                    .deviceInventoryNumber(device.getInventoryNumber())
                    .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            statusRepository.save(status);
            statusRepository.flush();

            controlRepository.save(control);
            controlRepository.flush();
            return true;
        }catch (Exception e){
            return false;
        }
    }


}
