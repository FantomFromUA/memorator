package com.memorator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "identifier is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
