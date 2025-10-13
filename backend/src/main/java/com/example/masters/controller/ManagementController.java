package com.example.masters.controller;

import com.example.masters.dto.ApiResponse;
import com.example.masters.dto.management.ActionRequest;
import com.example.masters.dto.management.MeasurementDto;
import com.example.masters.entity.Measurement;
import com.example.masters.service.ManagementService;
import com.example.masters.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/management")
@AllArgsConstructor
public class ManagementController {
    private final ManagementService managementService;
    private final MeasurementService measurementService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createAction(@Valid @RequestBody ActionRequest actionRequest) {
        boolean success = managementService.createAction(actionRequest);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", success));

    }

    @GetMapping("/sensors/{deviceId}")
    public ResponseEntity<ApiResponse<List<MeasurementDto>>> getSensorWithData(@PathVariable("deviceId") UUID deviceId,
                                                                               @RequestParam(required = false) String parameterName,
                                                                               @RequestParam(required = false) String from,
                                                                               @RequestParam(required = false) String to,
                                                                               @RequestParam(required = false) Double minValue,
                                                                               @RequestParam(required = false) Double maxValue) {
        List<MeasurementDto> measurementList = measurementService.getMeasurements(deviceId, parameterName, from, to, minValue, maxValue);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", measurementList));
    }
}
