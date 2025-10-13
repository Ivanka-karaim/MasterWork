package com.example.masters.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
    private UUID userId;
}
