package com.example.masters.security;

import com.example.masters.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // Access token дійсний 60 хвилин (60 * 60 * 1000 мс)
    @Value("${jwt.access.expiration:604800000}")
    private long ACCESS_TOKEN_EXPIRATION;

    // Refresh token дійсний 7 днів (7 * 24 * 60 * 60 * 1000 мс)
    @Value("${jwt.refresh.expiration:604800000}")
    private long REFRESH_TOKEN_EXPIRATION;


    public String generateAccessToken(UUID userId, Role role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("tokenType", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
    }


    public String getUserId(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }


    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    public String getTokenTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    public boolean isPasswordResetToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "PASSWORD_RESET".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
}