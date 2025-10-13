package com.example.masters.mqtt;

import com.example.masters.entity.Control;
import com.example.masters.entity.Device;
import com.example.masters.entity.Measurement;
import com.example.masters.entity.Status;
import com.example.masters.repository.MeasurementRepository;
import com.example.masters.repository.StatusRepository;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.service.RuleManagementService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttListenerService {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "SpringSubscriber";

    private MqttClient client;

    private final DeviceRepository deviceRepository;
    private final StatusRepository statusRepository;
    private final MeasurementRepository measurementRepository;
    private final RuleManagementService ruleManagementService;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            client = new MqttClient(BROKER, CLIENT_ID, new MqttDefaultFilePersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("Connection to broker lost", cause);
                }

                @Override
                @Transactional
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    log.info("Received message from {}: {}", topic, payload);

                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(payload);
                        if (topic.equals("laboratory/measurements")) {
                            String deviceInventoryNumber = root.has("deviceInventoryNumber") ? root.get("deviceInventoryNumber").asText() : null;
                            String parameterName = root.has("parameterName") ? root.get("parameterName").asText() : "UNKNOWN";
                            double value = root.has("value") ? root.get("value").asDouble() : 0;
                            LocalDateTime localTime = LocalDateTime.now();
                            Instant instant = localTime.atZone(ZoneId.systemDefault()).toInstant();
                            Timestamp utcTimestamp = Timestamp.from(instant);
                            Measurement measurement = Measurement.builder()
                                    .parameterName(parameterName)
                                    .value(value)
                                    .deviceInventoryNumber(deviceInventoryNumber)
                                    .dateTime(utcTimestamp)
                                    .build();

                            ruleManagementService.checkSensorRules(measurement);


                            measurementRepository.save(measurement);
                            measurementRepository.flush();

                            log.info("Saved measurement for device {} with payload {}", deviceInventoryNumber, payload);

                        } else if (topic.contains("status")) {
                            String[] parts = topic.split("/");
                            if (parts.length < 3) return;
                            String deviceInventoryNumber = parts[1];

                            String actionType = root.has("status") ? root.get("status").asText() : "UNKNOWN";
                            float actionValue = root.has("value") ? root.get("value").floatValue() : 0;

                            Status status = Status.builder()
                                    .actionType(actionType)
                                    .actionValue(actionValue)
                                    .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                                    .deviceInventoryNumber(deviceInventoryNumber)
                                    .build();

                            statusRepository.save(status);
                            statusRepository.flush();

                            log.info("Saved control action for device {} with payload {}", deviceInventoryNumber, payload);

                        }
                    } catch (Exception e) {
                        log.error("Error processing message from topic {}", topic, e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // not used for subscriber
                }
            });

            client.connect(options);
            client.subscribe("laboratory/measurements", 1);
            client.subscribe("laboratory/+/status", 1);
            log.info("Subscribed to topic: laboratory/+/status");

        } catch (MqttException e) {
            log.error("Error initializing MQTT listener", e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                log.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            log.error("Error disconnecting", e);
        }
    }
}
