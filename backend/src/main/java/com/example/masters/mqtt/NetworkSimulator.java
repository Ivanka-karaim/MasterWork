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
import java.util.concurrent.*;

@Service
@Slf4j
public class NetworkSimulator {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "NetworkSimulator";
    private static final String TOPIC = "laboratory/grid";

    private MqttClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;

    // --- симуляторні параметри ---
    private volatile boolean gridOnline = true;          // поточний стан мережі
    private volatile boolean autoMode = true;            // якщо true — випадкові переходи
    private final long publishIntervalSec = 60;          // як часто публікувати стан (сек)

    @PostConstruct
    public void start() {
        try {
            client = new MqttClient(BROKER, CLIENT_ID, new MqttDefaultFilePersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);

            scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(this::tick, 0, publishIntervalSec, TimeUnit.SECONDS);

            log.info("🌐 Network simulator started. Publishing to '{}'", TOPIC);
        } catch (Exception e) {
            log.error("Error starting NetworkSimulator", e);
        }
    }

    private void tick() {
        try {
            // якщо автоматична симуляція ввімкнена — з ймовірністю 10% змінюємо стан
            if (autoMode && random.nextDouble() < 0.10) {
                gridOnline = !gridOnline;
                log.info("🔁 Auto-toggle grid -> {}", gridOnline ? "ONLINE" : "OFFLINE");
            }

            // Публікуємо поточний стан як окремий JSON-параметр (format як у інших симуляторах)
            sendParam( "INV-006","INV-005", gridOnline ? "ONLINE" : "OFFLINE");

            log.info("🌐 Published GRID_STATUS = {}", gridOnline ? "ONLINE" : "OFFLINE");
        } catch (Exception e) {
            log.error("Error in NetworkSimulator.tick()", e);
        }
    }

    private void sendParam( String deviceInventoryNumber, String actionDeviceInventoryNumber, Object value) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceInventoryNumber", deviceInventoryNumber);
            payload.put("actionDeviceInventoryNumber", actionDeviceInventoryNumber);
            payload.put("value", value);

            String json = mapper.writeValueAsString(payload);
            MqttMessage msg = new MqttMessage(json.getBytes());
            msg.setQos(0);
            client.publish(TOPIC, msg);
        } catch (Exception e) {
            log.error("Error publishing GRID_STATUS", e);
        }
    }

    // REST / UI можуть викликати ці методи для ручного керування
    public void setGridOnline(boolean online) {
        this.gridOnline = online;
        // відразу опублікувати оновлення
        sendParam( "INV-006", "INV-005", gridOnline ? "ONLINE" : "OFFLINE");
        log.info("👉 Grid manually set to {}", gridOnline ? "ONLINE" : "OFFLINE");
    }

    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
        log.info("👉 Auto mode set to {}", autoMode);
    }

    public boolean isGridOnline() {
        return gridOnline;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    @PreDestroy
    public void stop() {
        try {
            if (scheduler != null) scheduler.shutdownNow();
            if (client != null && client.isConnected()) client.disconnect();
            log.info("Network simulator stopped");
        } catch (Exception e) {
            log.error("Error stopping NetworkSimulator", e);
        }
    }
}
