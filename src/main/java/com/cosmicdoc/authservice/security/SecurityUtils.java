package com.cosmicdoc.authservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;


/**
 * A utility class for accessing user information from the Spring Security context.
 * <p>
 * This provides static methods to easily retrieve details like the user ID and
 * organization ID from the authenticated user's JWT, avoiding the need to
 * pass the Authentication object through multiple layers of the application.
 */
public final class SecurityUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SecurityUtils() {
    }

    /**
     * Retrieves the JWT principal from the current security context.
     * The principal contains all the claims from the validated token.
     *
     * @return The Jwt object if the user is authenticated, otherwise null.
     */
    private static Jwt getPrincipal() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                return (Jwt) authentication.getPrincipal();
            }
        }
        return null;
    }

    /**
     * Gets the User ID (typically the 'sub' claim) of the currently authenticated user.
     *
     * @return The user ID string.
     * @throws SecurityException if the user is not authenticated or the claim is missing.
     */
    public static String getUserId() {
        Jwt principal = getPrincipal();
        if (principal != null) {
            return principal.getSubject(); // 'sub' claim is standard for user ID
        }
        throw new SecurityException("Could not retrieve User ID. No authenticated user found in security context.");
    }

    /**
     * Gets the Organization ID from the custom 'organizationId' claim of the currently
     * authenticated user's JWT.
     *
     * @return The organization ID string.
     * @throws SecurityException if the user is not authenticated or the claim is missing.
     */
    public static String getOrganizationId() {
        Jwt principal = getPrincipal();
        if (principal != null) {
            String organizationId = principal.getClaimAsString("organizationId");
            if (organizationId != null) {
                return organizationId;
            }
        }
        throw new SecurityException("Could not retrieve Organization ID. No authenticated user found or 'organizationId' claim missing.");
    }

    /**
     * Gets the Role from the custom 'role' claim of the currently authenticated user's JWT.
     *
     * @return The role string (e.g., "ROLE_SUPER_ADMIN").
     * @throws SecurityException if the user is not authenticated or the claim is missing.
     */
    public static String getRole() {
        Jwt principal = getPrincipal();
        if (principal != null) {
            String role = principal.getClaimAsString("role");
            if (role != null) {
                return role;
            }
        }
        throw new SecurityException("Could not retrieve Role. No authenticated user found or 'role' claim missing.");
    }

}