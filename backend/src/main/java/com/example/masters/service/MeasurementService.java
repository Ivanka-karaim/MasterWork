package com.example.masters.service;

import com.example.masters.dto.management.MeasurementDto;
import com.example.masters.entity.Device;
import com.example.masters.entity.Measurement;
import com.example.masters.exception.NotFoundException;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.MeasurementRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public List<MeasurementDto> getMeasurements(UUID deviceId,
                                             String parameterName,
                                             String fromDate,
                                             String toDate,
                                             Double minValue,
                                             Double maxValue) {

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("Датчик не знайдено"));

        Specification<Measurement> spec = (root, query, cb) -> cb.conjunction();
        Timestamp fromTs = (fromDate == null || fromDate.isBlank()) ? null : Timestamp.valueOf(fromDate.replace("T", " "));
        Timestamp toTs   = (toDate == null || toDate.isBlank()) ? null : Timestamp.valueOf(toDate.replace("T", " "));

        spec = spec
                .and(MeasurementSpecification.hasDeviceInventoryNumber(device.getInventoryNumber()))
                .and(MeasurementSpecification.hasParameterName(parameterName))
                .and(MeasurementSpecification.dateBetween(fromTs, toTs))
                .and(MeasurementSpecification.valueBetween(minValue, maxValue));

        return measurementRepository.findAll(spec).stream()
                .map(this::convertMeasurementToMeasurementDto)
                .collect(Collectors.toList());
    }
    private MeasurementDto convertMeasurementToMeasurementDto(Measurement measurement) {
        Timestamp timestamp = measurement.getDateTime();
        LocalDateTime localDateTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return MeasurementDto.builder()
                .id(measurement.getId())
                .value(measurement.getValue())
                .parameterName(measurement.getParameterName())
                .deviceInventoryNumber(String.valueOf(measurement.getDeviceInventoryNumber()))
                .dateTime(localDateTime)
                .build();
    }

    public static class MeasurementSpecification {

        public static Specification<Measurement> hasDeviceInventoryNumber(String deviceInventoryNumber) {
            return (root, query, cb) -> {
                if (deviceInventoryNumber == null || deviceInventoryNumber.isBlank()) return null;
                return cb.equal(root.get("deviceInventoryNumber"), deviceInventoryNumber);
            };
        }

        public static Specification<Measurement> hasParameterName(String parameterName) {
            return (root, query, cb) -> {
                if (parameterName == null || parameterName.isBlank()) return null;
                return cb.equal(root.get("parameterName"), parameterName);
            };
        }

        public static Specification<Measurement> dateBetween(Timestamp from, Timestamp to) {
            return (root, query, cb) -> {
                if (from == null && to == null) return null;
                else if (from == null) return cb.lessThanOrEqualTo(root.get("dateTime"), to);
                else if (to == null) return cb.greaterThanOrEqualTo(root.get("dateTime"), from);
                return cb.between(root.get("dateTime"), from, to);
            };
        }

        public static Specification<Measurement> valueBetween(Double min, Double max) {
            return (root, query, cb) -> {
                if (min == null && max == null) return null;
                else if (min == null) return cb.lessThanOrEqualTo(root.get("value"), max);
                else if (max == null) return cb.greaterThanOrEqualTo(root.get("value"), min);
                return cb.between(root.get("value"), min, max);
            };
        }
    }
}
