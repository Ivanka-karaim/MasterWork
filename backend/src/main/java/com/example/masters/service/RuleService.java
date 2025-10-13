package com.example.masters.service;

import com.example.masters.dto.rule.RuleRequest;
import com.example.masters.entity.Device;
import com.example.masters.entity.Rule;
import com.example.masters.entity.RuleHistory;
import com.example.masters.entity.User;
import com.example.masters.exception.NotFoundException;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.RuleHistoryRepository;
import com.example.masters.repository.RuleRepository;
import com.example.masters.security.GlobalResolver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RuleService {
    private final RuleRepository ruleRepository;
    private final DeviceRepository deviceRepository;
    private final GlobalResolver globalResolver;
    private final RuleHistoryRepository ruleHistoryRepository;

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    @Transactional(noRollbackFor = NotFoundException.class)
    public Rule createRule(RuleRequest request) {
        Device device = deviceRepository.findById(request.getDeviceId()).orElseThrow(() -> new NotFoundException("Пристрій не знайдено"));
        Device deviceAction = deviceRepository.findById(request.getActionDeviceId()).orElseThrow(() -> new NotFoundException("Пристрій не знайдено"));
        User user = globalResolver.requireCurrentUser();
        Rule rule = Rule.builder()
                .ruleType(request.getRuleType())
                .device(device)
                .operator(request.getOperator())
                .threshold(request.getThreshold())
                .actionDevice(deviceAction)
                .action(request.getActionType())
                .actionValue(request.getActionValue())
                .triggerDateTime(Timestamp.valueOf(request.getTriggerTime()))
                .active(true)
                .build();

        Rule newRule = ruleRepository.save(rule);
        ruleRepository.flush();

        RuleHistory history = RuleHistory.builder()
                .rule(newRule)
                .user(user)
                .action("CREATE")
                .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        ruleHistoryRepository.save(history);
        ruleHistoryRepository.flush();
        return newRule;
    }

    @Transactional(noRollbackFor = NotFoundException.class)
    public Rule updateRule(UUID id, RuleRequest request) {
        Device device = deviceRepository.findById(request.getDeviceId()).orElseThrow(() -> new NotFoundException("Пристрій не знайдено"));
        Device deviceAction = deviceRepository.findById(request.getActionDeviceId()).orElseThrow(() -> new NotFoundException("Пристрій не знайдено"));
        Rule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Правило не знайдено"));

        User user = globalResolver.requireCurrentUser();

        existing.setDevice(device);
        existing.setRuleType(request.getRuleType());
        existing.setOperator(request.getOperator());
        existing.setThreshold(request.getThreshold());
        existing.setActionDevice(deviceAction);
        existing.setAction(request.getActionType());
        existing.setActionValue(request.getActionValue());
        existing.setTriggerDateTime(Timestamp.valueOf(request.getTriggerTime()));

        Rule newRule = ruleRepository.save(existing);
        ruleRepository.flush();

        RuleHistory history = RuleHistory.builder()
                .rule(newRule)
                .user(user)
                .action("UPDATE")
                .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        ruleHistoryRepository.save(history);
        ruleHistoryRepository.flush();

        return newRule;
    }

    @Transactional(noRollbackFor = NotFoundException.class)
    public void deleteRule(UUID id) {
        User user = globalResolver.requireCurrentUser();

        Rule rule = ruleRepository.findById(id).orElseThrow(() -> new NotFoundException("Правило не знайдено"));
        RuleHistory ruleHistory = ruleHistoryRepository.findByRuleIdAndAction(id, "CREATE").orElseThrow(() -> new NotFoundException("Історію правил не знайдено"));
        if(user.getId().equals(ruleHistory.getUser().getId())) {
            ruleHistoryRepository.deleteByRuleId(id);
            ruleRepository.delete(rule);
        } else {
            rule.setActive(false);
            ruleRepository.save(rule);
            ruleRepository.flush();

            RuleHistory history = RuleHistory.builder()
                    .rule(rule)
                    .user(user)
                    .action("DELETE")
                    .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            ruleHistoryRepository.save(history);
            ruleHistoryRepository.flush();
        }

    }


}
