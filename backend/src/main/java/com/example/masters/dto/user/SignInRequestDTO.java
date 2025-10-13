package com.example.masters.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignInRequestDTO {
    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9._-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
}
