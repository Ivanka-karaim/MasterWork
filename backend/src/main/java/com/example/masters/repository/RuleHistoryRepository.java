package com.example.masters.repository;

import com.example.masters.entity.RuleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleHistoryRepository extends JpaRepository<RuleHistory, UUID> {
    Optional<RuleHistory> findByRuleIdAndAction(UUID ruleId, String action);
    List<RuleHistory> findByRuleIdOrderByDateTimeDesc(UUID ruleId);
    void deleteByRuleId(UUID ruleId);
}
