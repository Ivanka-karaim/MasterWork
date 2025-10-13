package com.example.masters.mqtt;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TemperatureSensorSimulator {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "TemperatureSimulator";
    private static final String TOPIC = "laboratory/measurements";

    private MqttClient client;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper();
    private ScheduledExecutorService scheduler;
    private double currentTemp = 22.0; // стартова температура

    @PostConstruct
    public void start() {
        try {
            client = new MqttClient(BROKER, CLIENT_ID, new MqttDefaultFilePersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::sendTemperature, 0, 30, TimeUnit.SECONDS);

            log.info("Temperature simulator started. Publishing to topic '{}'", TOPIC);
        } catch (Exception e) {
            log.error("Error starting temperature simulator", e);
        }
    }

    private void sendTemperature() {
        try {
            currentTemp += (random.nextDouble() - 0.5) * 0.4;
            currentTemp = Math.max(18, Math.min(28, currentTemp));

            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceInventoryNumber", "TEMP-001");
            payload.put("value", currentTemp);
            payload.put("parameterName", "TEMPERATURE");

            String json = mapper.writeValueAsString(payload);
            client.publish(TOPIC, new MqttMessage(json.getBytes()));

            log.info("📡 Sent temperature: {} °C", currentTemp);
        } catch (Exception e) {
            log.error("Error sending temperature", e);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (scheduler != null) scheduler.shutdownNow();
            if (client != null && client.isConnected()) client.disconnect();
            log.info("Temperature simulator stopped");
        } catch (Exception e) {
            log.error("Error stopping simulator", e);
        }
    }
}

