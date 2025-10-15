package com.example.masters.mqtt;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
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
public class BatterySimulator {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "BatterySimulator";
    private static final String TOPIC = "laboratory/measurements";

    private MqttClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;

    private double chargeLevel = 100.0; // поточний рівень заряду (%)
//    private double voltage = 12.5;      // напруга (В)
//    private double temperature = 25.0;  // температура батареї (°C)

    @PostConstruct
    public void start() {
        try {
            client = new MqttClient(BROKER, CLIENT_ID, new MqttDefaultFilePersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::simulateBattery, 0, 30, TimeUnit.SECONDS);

            log.info("🔋 Battery simulator started. Publishing to '{}'", TOPIC);
        } catch (Exception e) {
            log.error("Error starting battery simulator", e);
        }
    }

    private void simulateBattery() {
        try {
            // симулюємо зміну заряду
            chargeLevel += (random.nextDouble() - 0.5) * 3;
            chargeLevel = Math.max(0, Math.min(100, chargeLevel));

//            // симулюємо зміну напруги
//            voltage += (random.nextDouble() - 0.5) * 0.2;
//            voltage = Math.max(10.5, Math.min(13.0, voltage));
//
//            // симулюємо температуру
//            temperature += (random.nextDouble() - 0.5) * 0.5;
//            temperature = Math.max(20, Math.min(40, temperature));

            // Надсилаємо дані з батареї
            sendParam("INV-005", "LEVEL", String.format("%.1f", chargeLevel));
//            sendParam("INV-005", "VOLTAGE", String.format("%.2f", voltage));
//            sendParam("INV-005", "TEMPERATURE", String.format("%.1f", temperature));

            log.info("🔋 Battery data: charge={}%, U={}V, T={}°C",
                    String.format("%.1f", chargeLevel));
//                    String.format("%.2f", voltage),
//                    String.format("%.1f", temperature));

        } catch (Exception e) {
            log.error("Error simulating battery", e);
        }
    }

    private void sendParam(String deviceId, String paramName, Object value) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceInventoryNumber", deviceId);
            payload.put("parameterName", paramName);
            payload.put("value", value);

            String json = mapper.writeValueAsString(payload);
            client.publish(TOPIC, new MqttMessage(json.getBytes()));
        } catch (Exception e) {
            log.error("Error publishing parameter {}", paramName, e);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (scheduler != null) scheduler.shutdownNow();
            if (client != null && client.isConnected()) client.disconnect();
            log.info("Battery simulator stopped");
        } catch (Exception e) {
            log.error("Error stopping battery simulator", e);
        }
    }
}

