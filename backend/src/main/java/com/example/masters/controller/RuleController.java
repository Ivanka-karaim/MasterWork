package com.example.masters.controller;

import com.example.masters.dto.ApiResponse;
import com.example.masters.dto.rule.RuleRequest;
import com.example.masters.dto.rule.RuleResponse;
import com.example.masters.entity.Rule;
import com.example.masters.service.RuleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
@AllArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getAllRules(
            @RequestParam(required = false) List<String> ruleTypes,
            @RequestParam(required = false) List<UUID> deviceIds,
            @RequestParam(required = false) List<UUID> actionDeviceIds,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", ruleService.getAllRules(ruleTypes, deviceIds, actionDeviceIds, active)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RuleResponse>> createRule(@RequestBody RuleRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(200, "OK",  ruleService.createRule(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleResponse>> updateRule(@PathVariable UUID id, @RequestBody RuleRequest request) {
        return ResponseEntity.ok (new ApiResponse<>(200, "OK",  ruleService.updateRule(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK",  null));
    }
}

