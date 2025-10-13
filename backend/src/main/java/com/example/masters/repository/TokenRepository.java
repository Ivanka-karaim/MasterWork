package com.example.masters.repository;


import com.example.masters.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByJwtToken(String jwtToken);

    Optional<Token> findByRefreshToken(String refreshToken);

    @Query("SELECT t FROM Token t WHERE t.jwtToken = :token OR t.refreshToken = :token")
    Optional<Token> findByJwtTokenOrRefreshToken(String token, String token2);

    List<Token> findAllByUserId(UUID userId);

}
