package com.example.masters.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmokeSensorSimulator {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "SmokeSimulator";
    private static final String TOPIC = "laboratory/measurements";

    private MqttClient client;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper();
    private ScheduledExecutorService scheduler;
    private double currentSmoke = 25.0; // стартовий рівень диму (в ppm)

    @PostConstruct
    public void start() {
        try {
            client = new MqttClient(BROKER, CLIENT_ID, new MqttDefaultFilePersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::sendSmokeLevel, 0, 30, TimeUnit.SECONDS);

            log.info("Smoke simulator started. Publishing to topic '{}'", TOPIC);
        } catch (Exception e) {
            log.error("Error starting smoke simulator", e);
        }
    }

    private void sendSmokeLevel() {
        try {
            // коливання задимленості
            currentSmoke += (random.nextDouble() - 0.5) * 5;
            currentSmoke = Math.max(0, Math.min(300, currentSmoke));

            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceInventoryNumber", "SMOKE-001");
            payload.put("value", currentSmoke);
            payload.put("parameterName", "SMOKE_LEVEL");

            String json = mapper.writeValueAsString(payload);
            client.publish(TOPIC, new MqttMessage(json.getBytes()));

            String status = currentSmoke > 150 ? "⚠️ DANGEROUS" : "✅ Normal";
            log.info("💨 Sent smoke level: {} ppm [{}]", currentSmoke, status);

        } catch (Exception e) {
            log.error("Error sending smoke level", e);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (scheduler != null) scheduler.shutdownNow();
            if (client != null && client.isConnected()) client.disconnect();
            log.info("Smoke simulator stopped");
        } catch (Exception e) {
            log.error("Error stopping smoke simulator", e);
        }
    }
}