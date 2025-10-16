package com.example.masters.controller;

import com.example.masters.dto.ApiResponse;
import com.example.masters.dto.TokenResponse;
import com.example.masters.dto.user.SignInRequestDTO;
import com.example.masters.dto.user.SignUpRequestDTO;
import com.example.masters.dto.user.UpdatePasswordRequest;
import com.example.masters.dto.user.UserProfileResponse;
import com.example.masters.entity.User;
import com.example.masters.exception.UnauthorizedException;
import com.example.masters.security.TokenService;
import com.example.masters.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<Object>> signup(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        userService.registerUser(signUpRequestDTO);
        ApiResponse<Object> apiResponse = new ApiResponse<>(201, "OK", null);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<ApiResponse<TokenResponse>> signin(@Valid @RequestBody SignInRequestDTO authResponseDTO, HttpServletResponse response) {
        User user = userService.loginUser(authResponseDTO);
        if (user == null) {
            throw new UnauthorizedException("Invalid email or password");
        }
        TokenResponse tokenResponse = tokenService.generateToken(user);
        response.addHeader(HttpHeaders.SET_COOKIE, tokenService.createCookie(tokenResponse.getRefreshToken()).toString());
        ApiResponse<TokenResponse> apiResponse = new ApiResponse<>(200, "OK", tokenResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUsers() {

        UserProfileResponse userProfileResponse = userService.getUserProfileDTO();
        ApiResponse<UserProfileResponse> apiResponse = new ApiResponse<>(200, "OK", userProfileResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(@Valid @RequestBody SignUpRequestDTO userProfileUpdateRequest){
        UserProfileResponse userProfileResponse = userService.updateUserProfile(userProfileUpdateRequest);
        ApiResponse<UserProfileResponse> apiResponse = new ApiResponse<>(200, "OK", userProfileResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader, HttpServletResponse response) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Токен не надано");
        }
        String accessToken = authHeader.substring(7);
        tokenService.revokeSpecificToken(accessToken);
        response.addHeader(HttpHeaders.SET_COOKIE, tokenService.createCookie("").toString());
        return ResponseEntity.status(HttpStatus.OK).body("Користувач успішно вийшов із системи");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        List<UserProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", users));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{id}/update")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createUser(@PathVariable("id") UUID userId, @Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        UserProfileResponse userProfileResponse = userService.updateUserProfileForAdmin(userId, signUpRequestDTO);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", userProfileResponse));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", null));
    }




}
