package com.example.masters.entity;

import com.example.masters.entity.enums.Role;
import com.example.masters.entity.enums.Type;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "devices")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private String title;

    private String description;

    private String inventoryNumber;

    private Type type;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private byte[] image;

}
