package com.example.masters.service;

import com.example.masters.dto.device.DeviceSimpleResponse;
import com.example.masters.dto.rule.HistoryRuleResponse;
import com.example.masters.dto.rule.RuleRequest;
import com.example.masters.dto.rule.RuleResponse;
import com.example.masters.entity.Device;
import com.example.masters.entity.Rule;
import com.example.masters.entity.RuleHistory;
import com.example.masters.entity.User;
import com.example.masters.exception.ConflictException;
import com.example.masters.exception.NotFoundException;
import com.example.masters.repository.DeviceRepository;
import com.example.masters.repository.RuleHistoryRepository;
import com.example.masters.repository.RuleRepository;
import com.example.masters.repository.UserRepository;
import com.example.masters.security.GlobalResolver;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RuleService {
    private final RuleRepository ruleRepository;
    private final DeviceRepository deviceRepository;
    private final GlobalResolver globalResolver;
    private final RuleHistoryRepository ruleHistoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<RuleResponse> getAllRules(List<String> ruleTypes,
                                          List<UUID> deviceIds,
                                          List<UUID> actionDeviceIds,
                                          Boolean active) {
        return ruleRepository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (ruleTypes != null && !ruleTypes.isEmpty()) {
                        predicates.add(root.get("ruleType").in(ruleTypes));
                    }

                    if (deviceIds != null && !deviceIds.isEmpty()) {
                        predicates.add(root.get("device").get("id").in(deviceIds));
                    }

                    if (actionDeviceIds != null && !actionDeviceIds.isEmpty()) {
                        predicates.add(root.get("actionDevice").get("id").in(actionDeviceIds));
                    }

                    if (active != null) {
                        predicates.add(cb.equal(root.get("active"), active));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                }).stream()
                .map(this::convertRuleToRuleResponse)
                .collect(Collectors.toList());
    }

    private RuleResponse convertRuleToRuleResponse(Rule rule) {
        List<RuleHistory> histories = ruleHistoryRepository.findByRuleIdOrderByDateTimeDesc(rule.getId());
        return RuleResponse.builder()
                .id(rule.getId())
                .sensor(rule.getDevice() != null? convertDeviceToDeviceSimpleResponse(rule.getDevice()): null)
                .operator(rule.getOperator())
                .threshold(rule.getThreshold())
                .ruleType(rule.getRuleType())
                .active(rule.isActive())
                .action(rule.getAction())
                .actionValue(rule.getActionValue())
                .triggerTime(rule.getTriggerDateTime()!= null? rule.getTriggerDateTime().toLocalDateTime(): null)
                .actionDevice(convertDeviceToDeviceSimpleResponse(rule.getActionDevice()))
                .historyRules(histories.stream().map(this::convertHistoryToDTO).collect(Collectors.toList()))
                .strict(rule.isStrict())
                .build();
    }

    private HistoryRuleResponse convertHistoryToDTO(RuleHistory history) {
        return HistoryRuleResponse.builder()
                .dateTime(history.getDateTime().toLocalDateTime())
                .user(userService.convertUserToUserDTO(history.getUser()))
                .build();

    }

    private DeviceSimpleResponse convertDeviceToDeviceSimpleResponse(Device device) {
        return DeviceSimpleResponse.builder()
                .id(device.getId())
                .title(device.getTitle())
                .description(device.getDescription())
                .inventoryNumber(device.getInventoryNumber())
                .type(device.getType().toString())
                .build();
    }

    @Transactional(noRollbackFor = NotFoundException.class)
    public RuleResponse createRule(RuleRequest request) {
        Device device = null;
        if(request.getDeviceId()!=null) {
             device = deviceRepository.findById(request.getDeviceId()).orElseThrow(() -> new NotFoundException("Пристрій не знайдено"));
        }
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
                .triggerDateTime(request.getTriggerTime()!= null? Timestamp.valueOf(request.getTriggerTime()): null)
                .active(request.isActive())
                .strict(false)
                .build();

        Rule newRule = ruleRepository.save(rule);
        ruleRepository.flush();

        if(request.isMirrorRule()){
            String newOperator = request.getOperator() == ">"? "<":">";
            String newAction = request.getActionType() == "ON"?"OFF":"ON";
            Rule rule2 = Rule.builder()
                    .ruleType(request.getRuleType())
                    .device(device)
                    .operator(newOperator)
                    .threshold(request.getThreshold())
                    .actionDevice(deviceAction)
                    .action(newAction)
                    .actionValue(request.getActionValue())
                    .triggerDateTime(request.getTriggerTime()!= null? Timestamp.valueOf(request.getTriggerTime()): null)
                    .active(request.isActive())
                    .build();

            Rule newRule2 = ruleRepository.save(rule2);
            ruleRepository.flush();
        }

        RuleHistory history = RuleHistory.builder()
                .rule(newRule)
                .user(user)
                .action("CREATE")
                .dateTime(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        ruleHistoryRepository.save(history);
        ruleHistoryRepository.flush();
        return convertRuleToRuleResponse(newRule);
    }

    @Transactional(noRollbackFor = NotFoundException.class)
    public RuleResponse updateRule(UUID id, RuleRequest request) {
        Device device = null;
        if(request.getDeviceId()!=null) {
            device = deviceRepository.findById(request.getDeviceId()).orElseThrow(() -> new NotFoundException("Пристрій не знайдено"));
        }
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
        existing.setTriggerDateTime(request.getTriggerTime()!= null? Timestamp.valueOf(request.getTriggerTime()): null);
        existing.setActive(request.isActive());

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

        return convertRuleToRuleResponse(newRule);
    }

    @Transactional(noRollbackFor = NotFoundException.class)
    public void deleteRule(UUID id) {
        User user = globalResolver.requireCurrentUser();

        Rule rule = ruleRepository.findById(id).orElseThrow(() -> new NotFoundException("Правило не знайдено"));
        RuleHistory ruleHistory = ruleHistoryRepository.findByRuleIdAndAction(id, "CREATE").orElseThrow(() -> new NotFoundException("Історію правил не знайдено"));
        if (user.getId().equals(ruleHistory.getUser().getId())) {
            ruleHistoryRepository.deleteByRuleId(id);
            ruleRepository.delete(rule);
        } else {
            throw new ConflictException("Це не ваше правило, його неможливо видалити");

        }
    }


}
