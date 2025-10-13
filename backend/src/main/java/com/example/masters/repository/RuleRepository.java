package com.example.masters.repository;

import com.example.masters.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<Rule, UUID>, JpaSpecificationExecutor<Rule> {

    List<Rule> findByRuleTypeAndDeviceInventoryNumberAndActive(String type, String inventoryNumber, boolean active);
    List<Rule> findByRuleType(String type);
}
