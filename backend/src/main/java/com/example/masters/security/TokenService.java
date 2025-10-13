package com.example.masters.security;


import com.example.masters.dto.TokenResponse;
import com.example.masters.entity.Token;
import com.example.masters.entity.User;
import com.example.masters.exception.NotFoundException;
import com.example.masters.exception.UnauthorizedException;
import com.example.masters.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final TokenFactory tokenFactory;

    @Autowired
    public TokenService(TokenRepository tokenRepository, JwtUtil jwtUtil, TokenFactory tokenFactory) {
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
        this.tokenFactory = tokenFactory;
    }


    public Token saveUserTokens(User user, String accessToken, String refreshToken) {
        Token token = Token.builder()
                .user(user)
                .jwtToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        log.info("Saving tokens for user with ID: {}", user.getId());
        return tokenRepository.save(token);
    }


    @Transactional
    public void revokeAllUserId(UUID userId) {
        List<Token> validUserTokens = tokenRepository.findAllByUserId(userId);
        if (!validUserTokens.isEmpty()) {
            log.info("Revoking all tokens for user ID: {}", userId);
            tokenRepository.deleteAll(validUserTokens);
        }
    }


    public void revokeSpecificToken(String tokenValue) {
        Optional<Token> storedToken = tokenRepository.findByJwtTokenOrRefreshToken(tokenValue, tokenValue);
        if (storedToken.isPresent()) {
            log.info("Revoking specific token");
            tokenRepository.delete(storedToken.get());
        } else {
            log.warn("Attempted to revoke non-existent token");
            throw new NotFoundException("Token not found");
        }
    }

    public ResponseCookie createCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true) // тільки HTTPS
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60) // 7 днів
                .build();
    }


    public Optional<Token> findByRefreshToken(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken);
    }


    public Optional<Token> findByAccessToken(String accessToken) {
        return tokenRepository.findByJwtToken(accessToken);
    }


    public TokenResponse generateToken(User user) {
        log.info("Generating new token pair for user ID: {}", user.getId());
        return createAndSaveTokens(user);
    }


    public TokenResponse refreshTokens(String refreshToken) {
        log.debug("Attempting to refresh tokens");

        Optional<Token> tokenOptional = findByRefreshToken(refreshToken);
        validateRefreshToken(refreshToken, tokenOptional);

        User user = tokenOptional.get().getUser();
        log.info("Refreshing tokens for user ID: {}", user.getId());
        revokeSpecificToken(refreshToken);

        return createAndSaveTokens(user);
    }


    private void validateRefreshToken(String refreshToken, Optional<Token> tokenOptional) {
        if (tokenOptional.isEmpty()
                || !jwtUtil.validateToken(refreshToken)
                || !jwtUtil.isRefreshToken(refreshToken)
                || jwtUtil.getExpirationDateFromToken(refreshToken).before(new Date())) {
            log.warn("Refresh token is invalid or expired");
            tokenOptional.ifPresent(tokenRepository::delete);
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }


    private TokenResponse createAndSaveTokens(User user) {
        String accessToken = tokenFactory.createAccessToken(user.getId(), user.getRole());
        String refreshToken = tokenFactory.createRefreshToken(user.getId());
        saveUserTokens(user, accessToken, refreshToken);
        return tokenFactory.buildTokenResponse(accessToken, refreshToken, user.getId());
    }
}
