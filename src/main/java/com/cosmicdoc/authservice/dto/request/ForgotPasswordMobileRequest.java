package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the request body of the 'forgot password' endpoint using mobile.
 * Captures the mobile number of the user requesting a password reset.
 */
@Data
@NoArgsConstructor
public class ForgotPasswordMobileRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobile;
}
