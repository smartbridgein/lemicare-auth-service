package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the request body of the 'reset password' endpoint.
 * Captures the mobile number, OTP token, and the user's new desired password.
 */
@Data
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Mobile number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobile;
    
    @NotBlank(message = "OTP token cannot be blank")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String token;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    // For enhanced security, you could add a @Pattern annotation here
    // to enforce complexity rules (e.g., uppercase, numbers, special characters).
    private String password;
}