package com.example.masters.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity(name = "rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String ruleType; // SENSOR або TIME

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    private String operator;
    private Double threshold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_device_id")
    private Device actionDevice;
    private String action;
    private Double actionValue;

    private Timestamp triggerDateTime;

    private boolean active;
    private boolean strict;
}
