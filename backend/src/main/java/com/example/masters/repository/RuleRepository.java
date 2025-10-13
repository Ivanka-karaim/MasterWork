package com.example.masters.repository;

import com.example.masters.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID> {

    List<Rule> findByRuleTypeAndDeviceInventoryNumberAndActive(String type, String inventoryNumber, boolean active);
    List<Rule> findByRuleType(String type);
}
