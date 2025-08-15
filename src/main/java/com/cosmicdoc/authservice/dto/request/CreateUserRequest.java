package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Display name is required.")
    private String displayName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email format.")
    private String email;

    @NotBlank(message = "Role is required.")
    private String role; // e.g., "ROLE_DOCTOR", "ROLE_ADMIN"

    // Optional fields can be added here, like 'mobileNumber'
}