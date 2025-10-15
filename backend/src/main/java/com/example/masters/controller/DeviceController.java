package com.example.masters.controller;

import com.example.masters.dto.ApiResponse;
import com.example.masters.dto.control.ControlResponse;
import com.example.masters.dto.device.DeviceRequest;
import com.example.masters.dto.device.DeviceResponse;
import com.example.masters.dto.device.ModeRequest;
import com.example.masters.dto.management.MeasurementDto;
import com.example.masters.service.ControlService;
import com.example.masters.service.DeviceService;
import com.example.masters.service.MeasurementService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@AllArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final ControlService controlService;
    private final MeasurementService measurementService;

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getDevices(@PathVariable String type) {
        List<DeviceResponse> devices = deviceService.findAllByType(type);
        return ResponseEntity.ok(new ApiResponse<>(200,"OK",devices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDevice(@PathVariable UUID id) {
        DeviceResponse deviceResponse = deviceService.findDeviceById(id);
        return ResponseEntity.ok(new ApiResponse<>(200,"OK",deviceResponse));

    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<ControlResponse>>> getDeviceLogs(@PathVariable UUID id,
                                                                            @RequestParam(required = false) String fromDate, @RequestParam(required = false) String toDate) {
        List<ControlResponse> controlResponses = controlService.getAllActionsForDeviceId(id, fromDate, toDate);
        return ResponseEntity.ok(new ApiResponse<>(200,"OK",controlResponses));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getDeviceWithoutSensors() {
        List<DeviceResponse> deviceResponses = deviceService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(200,"OK",deviceResponses));
    }

    @GetMapping("/{id}/measurements")
    public ResponseEntity<ApiResponse<List<MeasurementDto>>> getDeviceMeasurements(@PathVariable UUID id) {
        List<MeasurementDto> measurementDtos = measurementService.getMeasurementsByDeviceId(id);
        return ResponseEntity.ok(new ApiResponse<>(200,"OK",measurementDtos));
    }

    @PutMapping("/{id}/mode")
    public ResponseEntity<ApiResponse<DeviceResponse>> editMode(@PathVariable UUID id, @RequestBody ModeRequest mode) {
        DeviceResponse deviceResponse = deviceService.editMode(id, mode.getMode());
        return ResponseEntity.ok(new ApiResponse<>(200,"OK",deviceResponse));
    }



}
