package com.example.masters.security;


import com.example.masters.dto.TokenResponse;
import com.example.masters.entity.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class TokenFactory {

    private final JwtUtil jwtUtil;

    @Autowired
    public TokenFactory(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    public String createAccessToken(UUID userId, Role role) {
        return jwtUtil.generateAccessToken(userId, role);
    }


    public String createRefreshToken(UUID userId) {
        return jwtUtil.generateRefreshToken(userId);
    }
    public TokenResponse buildTokenResponse(String accessToken, String refreshToken, UUID userId, String role) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userId)
                .role(role)
                .expiresIn(900) // seconds (15 minutes)
                .build();
    }
}
