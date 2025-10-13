package com.example.masters.security;



import com.example.masters.entity.User;
import com.example.masters.exception.NotFoundException;
import com.example.masters.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GlobalResolver {

    @Autowired
    private UserRepository userRepository;


    public User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            throw new NotFoundException("Authentication principal not found");
        }

        String principal = auth.getName();
        User user;

        UUID maybeId = tryParseUUID(principal);
        if (maybeId != null) {
            user = userRepository.findById(maybeId)
                    .orElseThrow(() -> new NotFoundException("User not found by id principal: " + principal));
        } else {
            user = userRepository.findByEmail(principal)
                    .orElseThrow(() -> new NotFoundException("User not found by principal: " + principal));
        }

        return user;
    }



    private UUID tryParseUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }
}