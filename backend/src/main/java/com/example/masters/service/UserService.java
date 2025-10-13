package com.example.masters.service;

import com.example.masters.dto.user.SignInRequestDTO;
import com.example.masters.dto.user.SignUpRequestDTO;
import com.example.masters.dto.user.UserProfileResponse;
import com.example.masters.entity.User;
import com.example.masters.entity.enums.Role;
import com.example.masters.exception.ConflictException;
import com.example.masters.exception.NotFoundException;
import com.example.masters.exception.UnauthorizedException;
import com.example.masters.exception.UnprocessableEntityException;
import com.example.masters.repository.UserRepository;
import com.example.masters.security.GlobalResolver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final GlobalResolver globalResolver;



    @Transactional(noRollbackFor = {UnprocessableEntityException.class, ConflictException.class})
    public void registerUser(SignUpRequestDTO dto) {
        log.info("Attempting to register user with email: {}", dto.getEmail());

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Password confirmation does not match for email: {}", dto.getEmail());
            throw new UnprocessableEntityException("Паролі не збігаються");
        }


        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email already exists: {}", dto.getEmail());
            throw new ConflictException("Електронна пошта вже зареєстрована раніше");
        }


        User user = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.valueOf(dto.getRole()))
                .build();

        userRepository.save(user);
        userRepository.flush();
        log.info("User registered successfully: {}", dto.getEmail());
    }

    @Transactional(noRollbackFor = {UnauthorizedException.class}, readOnly = true)
    public User loginUser(SignInRequestDTO signInRequestDTO) {
        log.info("Attempting to log in user with email: {}", signInRequestDTO.getEmail());

        User user = userRepository.findByEmail(signInRequestDTO.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", signInRequestDTO.getEmail());
                    return new UnauthorizedException("Неправильний логін або пароль");
                });

        if (!passwordEncoder.matches(signInRequestDTO.getPassword(), user.getPassword())) {
            log.warn("Invalid password for email: {}", signInRequestDTO.getEmail());
            throw new UnauthorizedException("Неправильний логін або пароль");
        }

        log.info("User logged in successfully: {}", signInRequestDTO.getEmail());
        return user;
    }

    @Transactional(noRollbackFor = {NotFoundException.class})
    public void changePassword(String email, String password) {
        log.info("Attempting to change password for email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            log.info("Password updated for user with phone: {}", email);
        } else {
            log.warn("User not found with phone: {}", email);
            throw new NotFoundException("Такого користувача не існує");
        }
    }


    @Transactional(noRollbackFor = {ConflictException.class, NotFoundException.class})
    public UserProfileResponse updateUserProfile(SignUpRequestDTO userProfileUpdateRequest) {

        User user = globalResolver.requireCurrentUser();

        if (user != null) {
            existByEmail(userProfileUpdateRequest.getEmail(), user);

            updateFieldIfNotEmpty(userProfileUpdateRequest.getFullName(), user::setFullName);
            updateFieldIfNotEmpty(userProfileUpdateRequest.getEmail(), user::setEmail);

            userRepository.save(user);
            userRepository.flush();
            log.info("User profile updated for userId={}", user.getId());
        } else {
            log.warn("User not found with userId={}", user.getId());
            throw new NotFoundException("Такого користувача не існуєd");
        }
        return convertUserToUserDTO(user);
    }


    @Transactional(readOnly = true, noRollbackFor = {NotFoundException.class})
    public UserProfileResponse getUserProfileDTO() {
        User user = globalResolver.requireCurrentUser();
        return convertUserToUserDTO(user);
    }

    private void updateFieldIfNotEmpty(String newValue, Consumer<String> setter) {
        if (newValue != null && !newValue.trim().isEmpty()) {
            setter.accept(newValue.trim());
        }
    }

    public void existByEmail(String email, User user) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            if (user.getEmail() == null || !email.equals(user.getEmail())) {
                log.warn("Email already exists: {}", email);
                throw new ConflictException("Електронна пошта вже зареєстрована раніше");
            }
        }
    }


    public UserProfileResponse convertUserToUserDTO(User user) {
        if (user == null) return null;

        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getValue())
                .build();
    }

}
