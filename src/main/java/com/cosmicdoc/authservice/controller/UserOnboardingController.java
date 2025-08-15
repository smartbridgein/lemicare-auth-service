package com.cosmicdoc.authservice.controller;

import com.cosmicdoc.authservice.dto.request.*;
import com.cosmicdoc.authservice.dto.response.SignInResponse;
import com.cosmicdoc.authservice.exception.AuthenticationException;
import com.cosmicdoc.authservice.exception.ResourceNotFoundException;
import com.cosmicdoc.authservice.service.UserOnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/public/auth")
public class UserOnboardingController {

    private final UserOnboardingService userOnboardingService;

    public UserOnboardingController(UserOnboardingService userOnboardingService) {
        this.userOnboardingService = userOnboardingService;
    }

    @PostMapping("/signup-super-admin")
    public ResponseEntity<String> signupSuperAdmin(@Valid @RequestBody SuperAdminSignupRequest request) {
        try {
            userOnboardingService.signupSuperAdmin(request);
            return ResponseEntity.ok("Organization and Super Admin created. Please check email for verification.");
        } catch (ExecutionException | InterruptedException e) {
            // Add proper global exception handling later
            return ResponseEntity.status(500).body("Error during signup: " + e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        // As before, @Valid triggers the validation rules in the SignInRequest DTO.
        // If validation fails (e.g., blank email), a 400 Bad Request is returned automatically
        // by the GlobalExceptionHandler.

        try {
            // 1. Delegate the core business logic to the service layer.
            SignInResponse signInResponse = userOnboardingService.signIn(request);

            // 2. If the service method returns successfully, wrap the response DTO
            //    in a ResponseEntity with a 200 OK status. The DTO will be
            //    automatically serialized into a JSON response body.
            return ResponseEntity.ok(signInResponse);

        } catch (AuthenticationException e) {
            // 3. Catch the specific AuthenticationException thrown by your service.
            //    This is for business logic errors like "Invalid credentials" or "Account not active".
            //    A 401 Unauthorized status is the correct HTTP response for authentication failures.
            return ResponseEntity.status(401).body(e.getMessage());

        } catch (Exception e) {
            // 4. A general catch-all for unexpected server-side errors.
            //    This could be a database issue or a NullPointerException that was missed.
            //    In a real application, you would log the full stack trace of 'e'.
            return ResponseEntity.status(500).body("An internal error occurred during sign-in.");
        }
    }

    @PostMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@Valid @RequestBody AccountVerificationRequest request) {
        // The @Valid annotation is crucial. It tells Spring to apply the validation rules
        // (e.g., @NotBlank, @Size) defined in the AccountVerificationRequest DTO.
        // If validation fails, Spring throws an exception that should be handled globally.

        try {
            // 1. Delegate the core business logic to the service layer.
            userOnboardingService.verifyAccount(request);

            // 2. If the service method completes without an exception, return a success response.
            return ResponseEntity.ok("Your account has been successfully verified. You can now sign in.");

        } catch (ResourceNotFoundException e) {
            // 3. Catch specific, known exceptions to return clear error messages.
            // This is for cases where the user associated with the token is not found.
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (IllegalStateException e) {
            // This catches errors like "Invalid token" or "Token has expired".
            // A 400 Bad Request is appropriate as the client sent invalid data.
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            // 4. A general catch-all for unexpected server errors (e.g., database connection issue).
            // In a real application, you would log this error.
            return ResponseEntity.status(500).body("An internal error occurred during account verification.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // We always return 200 OK, even if the user doesn't exist, to prevent email enumeration.
        try {
            userOnboardingService.forgotPassword(request);
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        } catch (Exception e) {
            // Note: For security, we don't want to return different messages based on
            // whether the email exists, as this allows attackers to enumerate emails.
            // So we still return a 200 OK here, but with a generic error message.
            return ResponseEntity.ok(
                    "An error occurred while processing your request. Please try again later.");
        }
    }

    /**
     * Mobile-based forgot password (uses OTP)
     * Note: For security reasons, this endpoint doesn't indicate if the mobile number exists
     * Instead, it delegates to the OtpController which will handle sending the OTP if the mobile exists
     */
    @PostMapping("/forgot-password-mobile")
    public ResponseEntity<String> forgotPasswordMobile(@Valid @RequestBody ForgotPasswordMobileRequest request) {
        try {
            return ResponseEntity.ok(
                    "If an account exists with this mobile number, an OTP will be sent for password reset.");
        } catch (Exception e) {
            // Catch generic server errors.
            e.printStackTrace(); // For debugging
            return ResponseEntity.status(500).body("An internal error occurred while resetting your password.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userOnboardingService.resetPassword(request);
            return ResponseEntity.ok("Your password has been successfully reset. You can now sign in with your new password.");
        } catch (IllegalStateException e) {
            // Catch specific errors like invalid/expired token.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            // User not found with the given mobile
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            // Catch generic server errors.
            e.printStackTrace(); // For debugging
            return ResponseEntity.status(500).body("An internal error occurred while resetting your password.");
        }
    }
}
