package com.example.masters.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MqttService {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "SpringPublisher";

    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            client.connect(options);
            log.info("Connected to broker");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String deviceInventoryNumber, String payload) {
        try {
            if (client != null && client.isConnected()) {
                String topic =  "laboratory/" + deviceInventoryNumber + "/control";
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                client.publish(topic, message);
                log.info("Message sent to {}: {}", topic, payload);
            } else {
                log.warn("MQTT client is not connected");
            }
        } catch (MqttException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
