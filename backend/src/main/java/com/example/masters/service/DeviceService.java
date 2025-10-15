package com.example.masters.service;

import com.example.masters.dto.device.DeviceResponse;
import com.example.masters.entity.Control;
import com.example.masters.entity.Device;
import com.example.masters.entity.Status;
import com.example.masters.entity.enums.Type;
import com.example.masters.exception.NotFoundException;
import com.example.masters.repository.ControlRepository;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.StatusRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final StatusRepository statusRepository;
    private final ControlRepository controlRepository;

    @Transactional(noRollbackFor = NotFoundException.class)
    public DeviceResponse editMode(UUID deviceId, String mode) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new NotFoundException("Не знайдено пристрій"));
        device.setMode(mode);
        deviceRepository.save(device);
        deviceRepository.flush();
        return convertToDeviceResponse(device);
    }

    @Transactional(readOnly = true)
    public DeviceResponse findDeviceById(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new NotFoundException("Не знайдено пристрій"));
        return convertToDeviceResponse(device);

    }

    @Transactional
    public List<DeviceResponse> findAll() {
        List<Device> devices = deviceRepository.findAll();
        return devices.stream()
                .map(this::convertToDeviceResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAllByType(String type) {
        Type typeEnum = Type.valueOf(type);
        List<Device> devices = deviceRepository.findByType(typeEnum);
        return devices.stream()
                .map(this::convertToDeviceResponse)
                .collect(Collectors.toList());

    }

    public DeviceResponse convertToDeviceResponse(Device device) {
        String imageBase64 = Base64.getEncoder().encodeToString(device.getImage());
        Status status = statusRepository.findFirstByDeviceInventoryNumberOrderByDateTimeDesc(device.getInventoryNumber()).orElse(null);
        Control control = controlRepository.findFirstByDeviceIdOrderByDateTimeDesc(device.getId()).orElse(null);
        return DeviceResponse.builder()
                .id(device.getId())
                .title(device.getTitle())
                .description(device.getDescription())
                .inventoryNumber(device.getInventoryNumber())
                .type(device.getType().getValue())
                .image(imageBase64)
                .isOn(status != null && Objects.equals(status.getActionType(), "ON"))
                .mode(device.getMode())
                .build();

    }

}
