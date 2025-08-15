package com.cosmicdoc.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the data returned to the client after a successful sign-in.
 * It includes the authentication token and essential user context to build
 * the initial UI without requiring immediate follow-up API calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponse {

    /**
     * The JSON Web Token (JWT) that must be included in the Authorization header
     * for all subsequent authenticated requests.
     */
    private String token;

    /**
     * The unique ID of the authenticated user.
     */
    private String userId;

    /**
     * The display name of the user (e.g., "Dr. Priya Sharma").
     * Used for welcome messages in the UI.
     */
    private String displayName;

    /**
     * The user's role within the logged-in organization (e.g., "ROLE_SUPER_ADMIN").
     * The UI uses this to show/hide admin menus and features.
     */
    private String role;

    /**
     * A list of organization IDs the user belongs to.
     * This is crucial for allowing the user to switch between organizations
     * if they are a member of more than one.
     */
    private List<String> organizations;
}