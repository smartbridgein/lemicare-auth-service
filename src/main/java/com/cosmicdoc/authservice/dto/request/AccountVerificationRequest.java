package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the request body of the account verification endpoint.
 * This class captures the verification token sent to the user's email
 * and the new password they wish to set for their account.
 */
@Data
@NoArgsConstructor
public class AccountVerificationRequest {

    /**
     * The secure, unique token that was generated during signup and sent to the user.
     * This token identifies the pending user account.
     */
    @NotBlank(message = "Verification token is required.")
    private String verificationToken;

    /**
     * The user's chosen password.
     * We apply validation rules to enforce a strong password policy.
     */
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    // You can add a @Pattern annotation for more complex rules, e.g., requiring
    // uppercase, lowercase, numbers, and special characters.
    // Example: @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
    //                  message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.")
    private String password;
}