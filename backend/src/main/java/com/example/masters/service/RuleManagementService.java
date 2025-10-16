package com.example.masters.service;

import com.example.masters.dto.notification.NotificationRequest;
import com.example.masters.entity.Measurement;
import com.example.masters.entity.Rule;
import com.example.masters.entity.Status;
import com.example.masters.entity.User;
import com.example.masters.entity.enums.Role;
import com.example.masters.exception.BadRequestException;
import com.example.masters.mqtt.MqttService;
import com.example.masters.repository.MeasurementRepository;
import com.example.masters.repository.RuleRepository;
import com.example.masters.repository.StatusRepository;
import com.example.masters.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final StatusRepository statusRepository;
    private final MeasurementRepository measurementRepository;


    @Transactional
    public void checkSensorRules(Measurement measurement) {
        List<Rule> rules = ruleRepository.findByRuleTypeAndDeviceInventoryNumberAndActive("SENSOR", measurement.getDeviceInventoryNumber(), true);
        Double measurementValue = measurementRepository.getAverageOfLastTen(measurement.getDeviceInventoryNumber(), measurement.getParameterName());
        System.out.println(measurementValue);

        for (Rule rule : rules) {
            Status status = statusRepository.findFirstByDeviceInventoryNumberOrderByDateTimeDesc(rule.getActionDevice().getInventoryNumber()).orElse(null);

            if (isConditionMet(rule, measurementValue) && (status == null || !Objects.equals(status.getActionType(), rule.getAction()))) {
                boolean sendMessage = executeAction(rule);
                if (!sendMessage) {
                    throw new BadRequestException("Уппссс, щось пішло не так");
                } else{
                    sendMessageForAdminForSensorRule(rule, measurementValue);
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
                    "value", rule.getActionValue() == null? 0: rule.getActionValue()
            ));

//TODO коли система буде працювати це не потрібно
            Status status = Status.builder()
                    .actionType(rule.getAction())
                    .actionValue(0)
                    .deviceInventoryNumber(rule.getActionDevice().getInventoryNumber())
                    .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            statusRepository.save(status);
            statusRepository.flush();

            mqttService.sendCommand(rule.getActionDevice().getInventoryNumber(), payload);


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void sendMessageForAdminForSensorRule(Rule rule, double value) {
        NotificationRequest request = NotificationRequest.builder()
                .title( rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ? " увімкнено" : " вимкнено"))
                .message("Пристрій " + rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ?
                        " було увімкнено" : " було вимкнено")+", оскільки показник датчика " + rule.getDevice().getTitle() +
                        " " + rule.getOperator() + " " + rule.getThreshold() + "\nА саме в середньому становить: " + value)
                        .build();
        List<User> users = userRepository.findByRole(Role.ADMIN);
        for (User user : users) {
            notificationService.createNotification(user.getId(), request);
        }
    }
    private void sendMessageForAdminForTime(Rule rule) {
        NotificationRequest request = NotificationRequest.builder()
                .title( rule.getActionDevice().getTitle() + (Objects.equals(rule.getAction(), "ON") ? " увімкнено" : " вимкнено"))
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
