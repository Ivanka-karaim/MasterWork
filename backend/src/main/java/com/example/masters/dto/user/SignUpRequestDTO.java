package com.example.masters.dto.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignUpRequestDTO {

    private String fullName;


    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9._-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;


    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, and 1 number"
    )
    private String password;

    private String confirmPassword;

    private String role;
}
