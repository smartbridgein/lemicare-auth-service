package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Display name is required.")
    private String displayName;

    @NotBlank(message = "Role is required.")
    private String role; // e.g., "ROLE_DOCTOR", "ROLE_STAFF"

    // You could also add other updatable fields like 'mobileNumber'.
}