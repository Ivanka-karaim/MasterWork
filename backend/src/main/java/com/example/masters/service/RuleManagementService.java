package com.example.masters.service;

import com.example.masters.dto.notification.NotificationRequest;
import com.example.masters.entity.Measurement;
import com.example.masters.entity.Rule;
import com.example.masters.entity.User;
import com.example.masters.entity.enums.Role;
import com.example.masters.exception.BadRequestException;
import com.example.masters.mqtt.MqttService;
import com.example.masters.repository.RuleRepository;
import com.example.masters.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class RuleManagementService {
    private final RuleRepository ruleRepository;
    private final MqttService mqttService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    public void checkSensorRules(Measurement measurement) {
        List<Rule> rules = ruleRepository.findByRuleTypeAndDeviceInventoryNumberAndActive("SENSOR", measurement.getDeviceInventoryNumber(), true);
        for (Rule rule : rules) {
            if (isConditionMet(rule, measurement.getValue())) {
                boolean sendMessage = executeAction(rule);
                if (!sendMessage) {
                    throw new BadRequestException("Уппссс, щось пішло не так");
                } else{
                    sendMessageForAdminForSensorRule(rule, measurement.getValue());
                }
            }
        }
    }

    private boolean isConditionMet(Rule rule, double value) {
        return switch (rule.getOperator()) {
            case ">" -> value > rule.getThreshold();
            case "<" -> value < rule.getThreshold();
            case "=" -> value == rule.getThreshold();
            default -> false;
        };
    }

    private boolean executeAction(Rule rule) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String payload = mapper.writeValueAsString(Map.of(
                    "action", rule.getAction(),
                    "value", rule.getActionValue()
            ));

            mqttService.sendCommand(rule.getActionDevice().getInventoryNumber(), payload);


        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void sendMessageForAdminForSensorRule(Rule rule, double value) {
        NotificationRequest request = NotificationRequest.builder()
                .title("Пристрій " + rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ? " увімкнено" : " вимкнено"))
                .message("Пристрій " + rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ?
                        " було увімкнено" : " було вимкнено")+", оскільки показник датчика " + rule.getDevice().getTitle() +
                        " " + rule.getOperator() + " " + rule.getThreshold() + "\nА саме становить: " + value)
                        .build();
        List<User> users = userRepository.findByRole(Role.ADMIN);
        for (User user : users) {
            notificationService.createNotification(user.getId(), request);
        }
    }
    private void sendMessageForAdminForTime(Rule rule) {
        NotificationRequest request = NotificationRequest.builder()
                .title("Пристрій " + rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ? " увімкнено" : " вимкнено"))
                .message("Пристрій " + rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ?
                        " було увімкнено" : " було вимкнено") + " о " + rule.getTriggerDateTime()+" за створеним правилом")
                .build();
        List<User> users = userRepository.findByRole(Role.ADMIN);
        for (User user : users) {
            notificationService.createNotification(user.getId(), request);
        }
    }


    // Викликається щохвилини (через @Scheduled)
    @Scheduled(cron = "0 * * * * *") // кожну хвилину
    public void checkTimeRules() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<Rule> timeRules = ruleRepository.findByRuleType("TIME");

        for (Rule rule : timeRules) {
            if (rule.getTriggerDateTime() != null && rule.getTriggerDateTime().equals(Timestamp.valueOf(now))) {
                boolean sendMassage = executeAction(rule);
                if(sendMassage){
                    sendMessageForAdminForTime(rule);
                }
            }
        }
    }


}
