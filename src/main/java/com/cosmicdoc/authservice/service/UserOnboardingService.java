package com.cosmicdoc.authservice.service;

import com.cosmicdoc.authservice.dto.request.*;
import com.cosmicdoc.authservice.dto.response.SignInResponse;
import com.cosmicdoc.authservice.exception.AuthenticationException;
import com.cosmicdoc.authservice.exception.ResourceNotFoundException;
import com.cosmicdoc.authservice.security.JwtService;
import com.cosmicdoc.common.model.*;
import com.cosmicdoc.common.repository.*;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
import com.google.cloud.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import com.google.cloud.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


/**
 * Service class handling the core logic for user onboarding and authentication.
 * This includes registration, verification, sign-in, and password management.
 */
@Service
@RequiredArgsConstructor // Lombok annotation for clean constructor dependency injection
public class UserOnboardingService {

    // Repositories from your shared JAR (implementations are local)
    private final UsersRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserMembershipService userMembershipService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Firestore firestore;
    private final OtpService otpService;
    private final JwtService jwtService;

    // TODO: You would also inject a VerificationTokenRepository for verify/reset flows.

    /**
     * Handles the complete signup process for a new Super Admin and their organization.
     * This is a transactional operation that creates multiple documents in Firestore.
     *
     * @param request The signup request containing organization and user details.
     * @throws IllegalStateException if the email or organization name already exists.
     * @throws ExecutionException | InterruptedException if the Firestore operation fails.
     */
   // @Transactional // Ensures all database writes within this method are atomic
    public void signupSuperAdmin(SuperAdminSignupRequest request) throws ExecutionException, InterruptedException {
        // 1. Pre-condition validation: Ensure email and organization are unique.
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists.");
        }
        if (organizationRepository.findByOrganizationName(request.getOrganizationName()).isPresent()) {
            throw new IllegalStateException("An organization with this name already exists.");
        }

        // 2. Generate unique, readable IDs for the new documents.
        String orgId = "org_" + UUID.randomUUID().toString();
        String userId = "user_" + UUID.randomUUID().toString();
        String branchId = "branch_" + UUID.randomUUID().toString();

        // 3. Create the domain model objects from the request DTO.
        Users newUser = Users.builder()
                .userId(userId)
                .email(request.getEmail())
                // User will set their password upon verification. Store a secure, non-loginable hash for now.
                .hashedPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .status(UserStatus.PENDING_VERIFICATION)
                .displayName(request.getEmail().split("@")[0]) // A sensible default display name
                .mobileNumber(request.getMobileNumber())
                .organizations(Collections.singletonList(orgId))
                .build();

        Organization newOrg = Organization.builder()
                .orgId(orgId)
                .name(request.getOrganizationName())
                .normalizedName(request.getOrganizationName().toLowerCase().trim())
                .status("ACTIVE") // The org is active immediately
                .hasMultipleBranches(request.isHasMultipleBranches())
                .build();

        Branch initialBranch = Branch.builder()
                .branchId(branchId)
                .name(request.getInitialBranchName())
                .address(request.getAddress()) // Default value
                .build();

        OrganizationMember membership = OrganizationMember.builder()
                .userId(userId)
                .organizationId(orgId)
                .role("ROLE_SUPER_ADMIN")
                .accessType("ORG_WIDE")
                .build();

        String tokenString = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(tokenString)
                .userId(userId)
                .email(request.getEmail())
                .expiresAt(Timestamp.ofTimeSecondsAndNanos(
                        Instant.now().plus(24, ChronoUnit.HOURS).getEpochSecond(), 0)) // 24-hour expiry
                .build();

        // 4. Use a Firestore WriteBatch to perform an atomic multi-document transaction.
        WriteBatch batch = firestore.batch();

        userRepository.saveInTransaction(batch, newUser);
        organizationRepository.saveInTransaction(batch, newOrg);
        branchRepository.saveInTransaction(batch, orgId, initialBranch);
        memberRepository.saveInTransaction(batch, membership);
        verificationTokenRepository.saveInTransaction(batch,verificationToken);

        batch.commit().get();

        // 6. Post-transaction action: Send a verification email.
        // TODO: In a real app, you would generate a secure verification token,
        //       save it to a 'verification_tokens' collection with a TTL,
        //       and call a NotificationService to send the email with the token.
        notificationService.sendVerificationEmail(request.getEmail(), tokenString);
    }

    /**
     * Authenticates a user and returns a JWT if credentials are valid.
     *
     * @param request The sign-in request with email and password.
     * @return A SignInResponse containing the JWT.
     * @throws AuthenticationException if credentials are invalid or user is not active.
     */
    public SignInResponse signIn(SignInRequest request) throws AuthenticationException {
        try {
            System.out.println("DEBUG: Starting authentication for email: " + request.getEmail());
            
            // 1. Find the user by email. Throw a generic error to prevent email enumeration attacks.
            Users user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password."));
            
            System.out.println("DEBUG: User found: " + user.getUserId());

            // 2. Verify the user's account status.
            if (user.getStatus() != UserStatus.ACTIVE) {
                System.out.println("DEBUG: User status check failed. Status: " + user.getStatus());
                throw new AuthenticationException("User account is not active. Please verify your email or contact support.");
            }
            
            System.out.println("DEBUG: User status is active");

            // 3. Securely compare the provided password with the stored hash.
            System.out.println("DEBUG: Retrieved hashed password: " + 
                (user.getHashedPassword() == null ? "NULL" : 
                 (user.getHashedPassword().isEmpty() ? "EMPTY" : 
                  "LENGTH=" + user.getHashedPassword().length())));
            System.out.println("DEBUG: Input password: " + 
                (request.getPassword() == null ? "NULL" : 
                 (request.getPassword().isEmpty() ? "EMPTY" : 
                  "LENGTH=" + request.getPassword().length())));
                  
            // Check explicitly for null or empty password hash
            if (user.getHashedPassword() == null || user.getHashedPassword().isEmpty()) {
                System.out.println("DEBUG: Password verification failed - stored hash is null or empty");
                throw new AuthenticationException("Invalid email or password - missing password hash.");
            }
            
            System.out.println("DEBUG: Comparing password hash using BCrypt");
            
            try {
                System.out.println("DEBUG: About to compare passwords");
                System.out.println("DEBUG: Input password: '" + request.getPassword() + "'");
                System.out.println("DEBUG: Stored hash: '" + user.getHashedPassword() + "'");
                
                boolean matches = passwordEncoder.matches(request.getPassword(), user.getHashedPassword());
                System.out.println("DEBUG: Password match result: " + matches);
                
                if (!matches) {
                    System.out.println("DEBUG: Password verification failed - BCrypt mismatch");
                    throw new AuthenticationException("Invalid email or password.");
                }
                
                System.out.println("DEBUG: Password verification passed");
            } catch (Exception e) {
                System.out.println("DEBUG: Exception during password comparison: " + e.getMessage());
                e.printStackTrace();
                throw new AuthenticationException("Error during password verification: " + e.getMessage());
            }

            // 4. A user can belong to multiple orgs. For a simple sign-in, we log them into their first org.
            //    A more advanced implementation might ask the user which org to sign into if they have > 1.
            //    If the user has no organizations, we'll create a basic JWT without org-specific claims
            String defaultOrgId = null;
            OrganizationMember membership = null;
            
            if (user.getOrganizations() != null && !user.getOrganizations().isEmpty()) {
                defaultOrgId = user.getOrganizations().stream().findFirst().get();
                System.out.println("DEBUG: Found default organization: " + defaultOrgId);

                membership = memberRepository.findByUserIdAndOrgId(user.getUserId(), defaultOrgId)
                        .orElse(null);
                        
                if (membership != null) {
                    System.out.println("DEBUG: Found membership for user in organization");
                } else {
                    System.out.println("DEBUG: No membership found for user in organization: " + defaultOrgId);
                    // Create a default membership for authentication purposes
                    membership = new OrganizationMember();
                    membership.setUserId(user.getUserId());
                    membership.setOrganizationId(defaultOrgId);
                    membership.setRole("USER"); // Default role
                }
            } else {
                System.out.println("DEBUG: User has no organizations, proceeding with basic authentication");
                // Create a temporary membership object for the JWT generation
                membership = new OrganizationMember();
                membership.setUserId(user.getUserId());
                membership.setOrganizationId("NO_ORG"); // Special marker
                membership.setRole("USER"); // Default role
            }

            // 5. Generate the JWT with user and tenant context.
            String token = jwtService.generateToken(user, membership);
            System.out.println("DEBUG: JWT token generated successfully");

            // 6. Update the last login timestamp (fire-and-forget is acceptable here).
            userRepository.updateLastLogin(user.getUserId());
            System.out.println("DEBUG: Last login timestamp updated");

            // 7. Build the rich SignInResponse object with safety checks
            SignInResponse.SignInResponseBuilder responseBuilder = SignInResponse.builder()
                    .token(token)
                    .userId(user.getUserId())
                    .displayName(user.getDisplayName());
                    
            // Add organization information if available
            if (membership != null && membership.getRole() != null) {
                responseBuilder.role(membership.getRole());
            } else {
                responseBuilder.role("USER"); // Default role if none available
            }
            
            // Add organizations if available, otherwise empty list
            if (user.getOrganizations() != null) {
                responseBuilder.organizations(user.getOrganizations());
            }
            
            SignInResponse response = responseBuilder.build();
                    
            System.out.println("DEBUG: Sign-in response built successfully");
            return response;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception during sign-in: " + e.getMessage());
            e.printStackTrace();
            throw new AuthenticationException("An internal error occurred during sign-in.");
        }

    }

    /**
     * Verifies a user's account and sets their initial password.
     *
     * @param request The verification request with token and new password.
     */
    public void verifyAccount(AccountVerificationRequest request) throws ResourceNotFoundException {
        // TODO: Implement logic
        // 1. Find the verification token in your 'verificationtokens' collection.
        // 2. If not found or expired, throw an exception.
        // 3. Get the userId associated with the token.
        // 4. Fetch the User object.
        // 5. Hash the new password from the request: passwordEncoder.encode(request.getPassword())
        // 6. Update the user's document: set hashedPassword and change status to ACTIVE.
        // 7. Delete the verification token so it cannot be reused.
        // 8. This entire process should be transactional (use a WriteBatch).
        /**
         * Verifies a user's account using a token and sets their permanent password.
         * This is an atomic operation.
         *
         * @param request The verification request DTO.
         * @throws IllegalStateException if the token is invalid or expired.
         * @throws ResourceNotFoundException if the user associated with the token is not found.
         */
         // 1. Find the verification token in the database.
            VerificationToken token = verificationTokenRepository.findByToken(request.getVerificationToken())
                    .orElseThrow(() -> new IllegalStateException("Invalid verification token."));

            // 2. Check if the token has expired.
            if (token.getExpiresAt().compareTo(Timestamp.now()) < 0) {
                // Optional: You could also delete the expired token here.
                throw new IllegalStateException("Verification token has expired. Please request a new one.");
            }

            // 3. Find the user associated with the token.
            String userId = token.getUserId();
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for the given token."));

            // 4. Prepare the updates for the user document.
            String newHashedPassword = passwordEncoder.encode(request.getPassword());

            // Use a Map for a targeted update to avoid race conditions.
            // This only changes the 'hashedPassword' and 'status' fields.
            java.util.Map<String, Object> userUpdates = new java.util.HashMap<>();
            userUpdates.put("hashedPassword", newHashedPassword);
            userUpdates.put("status", UserStatus.ACTIVE);

            // 5. Perform the user update and token deletion in a single atomic transaction.
            WriteBatch batch = firestore.batch();

            var userDocRef = firestore.collection("users").document(userId);
            batch.update(userDocRef, userUpdates); // Update the user document

            // Use the repository method to delete the token within the same transaction
            verificationTokenRepository.deleteInTransaction(batch, token.getToken());

            try {
                // Commit the batch. Both operations succeed or both fail.
                batch.commit().get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify account.", e);
            }
        }

    /**
     * Initiates the password reset process for a given email address.
     * <p>
     * This method is designed to be secure against email enumeration attacks. It will
     * always return a success-like response to the client, regardless of whether the
     * email exists in the system. If the user exists, a token is generated, stored,
     * and conceptually sent via email.
     *
     * @param request The DTO containing the user's email.
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        // 1. Look up the user by email.
        Optional<Users> userOptional = userRepository.findByEmail(request.getEmail());

        // 2. Security Check: If no user is found, we immediately exit.
        //    We DO NOT throw an error. This prevents attackers from using this endpoint
        //    to discover which email addresses are registered in our application.
        if (userOptional.isEmpty()) {
            return; // Exit silently
        }

        Users user = userOptional.get();

        // 3. Generate a secure, unique token for the password reset request.
        String tokenString = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenString)
                .userId(user.getUserId())
                .email(user.getEmail())
                // Set an expiration time, typically short (e.g., 1 hour).
                .expiresAt(Timestamp.of(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))))
                .build();


        // 4. Save the token to the database. The user will use this token to prove ownership.
        passwordResetTokenRepository.save(resetToken);

        // 5. Send the notification.
        // TODO: In a real application, this is where you would call a separate NotificationService
        //       to send an email to the user. The email would contain a link like:
        //       https://your-frontend-app.com/reset-password?token={tokenString}
        System.out.println("--- PASSWORD RESET ---");
        System.out.println("Token for " + user.getEmail() + " is: " + tokenString);
        System.out.println("--------------------");
        notificationService.sendPasswordResetEmail(user.getEmail(), tokenString);
    }

    /**
     * Completes the password reset process using a valid token and a new password.
     * <p>
     * This is a transactional operation. It will update the user's password and
     * delete the reset token in a single atomic batch. If any part fails, the
     * entire operation is rolled back.
     *
     * @param request The DTO containing the reset token and the new password.
     * @throws IllegalStateException if the provided token is not found or has expired.
     * @deprecated This method is for email-based password reset. Use the OTP-based method instead.
     */
    @Deprecated
    public void resetPasswordWithEmail(ResetPasswordRequest request) {
        // 1. Find the token document in the database.
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalStateException("This password reset token is invalid."));

        // 2. Validate that the token has not expired.
        if (token.getExpiresAt().compareTo(Timestamp.now()) < 0) {
            throw new IllegalStateException("This password reset token has expired. Please request a new one.");
        }

        // 3. Securely hash the new password provided by the user.
        String newHashedPassword = passwordEncoder.encode(request.getPassword());

        // 4. Create a Map for the partial update. This is efficient and avoids race conditions,
        //    as it only modifies the 'hashedPassword' field.
        Map<String, Object> userUpdates = Map.of("hashedPassword", newHashedPassword);

        // 5. Perform the user update and token deletion in a single atomic transaction.
        WriteBatch batch = firestore.batch();

        var userDocRef = firestore.collection("users").document(token.getUserId());
        batch.update(userDocRef, userUpdates); // Operation 1: Update the user's password

        var tokenDocRef = firestore.collection("password_reset_tokens").document(token.getToken());
        batch.delete(tokenDocRef); // Operation 2: Delete the token so it cannot be reused

        try {
            // 6. Commit the batch. Both operations will succeed or both will fail.
            batch.commit().get();
        } catch (Exception e) {
            // If the transaction fails, wrap it in a standard runtime exception.
            throw new RuntimeException("Failed to process password reset.", e);
        }
    }
    
    /**
     * Resets a user's password using mobile number and OTP verification.
     * <p>
     * This method verifies the OTP sent to the user's mobile number and then updates
     * the user's password if the OTP is valid.
     *
     * @param request The DTO containing the mobile number, OTP, and new password.
     * @throws IllegalStateException if the OTP is invalid or expired.
     * @throws ResourceNotFoundException if no user is found with the given mobile number.
     */
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Validate the OTP
        boolean isValidOtp = otpService.validateOtp(request.getMobile(), request.getToken(), "reset");
        if (!isValidOtp) {
            throw new IllegalStateException("Invalid or expired OTP. Please request a new one.");
        }
        
        // 2. Find the user by mobile number
        Optional<Users> userOptional = userRepository.findByPhone(request.getMobile());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("No user found with this mobile number.");
        }
        
        Users user = userOptional.get();
        
        // 3. Hash the new password
        String newHashedPassword = passwordEncoder.encode(request.getPassword());
        
        // 4. Update the user's password
        user.setHashedPassword(newHashedPassword);
        
        // 5. Save the updated user
        try {
            userRepository.save(user);
            System.out.println("Password reset successful for user: " + user.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset password: " + e.getMessage(), e);
        }
    }

  }



