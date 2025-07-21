package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the request body of the 'forgot password' endpoint.
 * Captures the email address of the user requesting a password reset.
 */
@Data
@NoArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email format.")
    private String email;
}