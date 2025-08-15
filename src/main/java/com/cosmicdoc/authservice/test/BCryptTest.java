package com.cosmicdoc.authservice.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Simple utility class to test BCrypt password matching.
 */
public class BCryptTest {
    public static void main(String[] args) {
        // Create a BCryptPasswordEncoder with default settings (same as in SecurityConfig)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // The stored hash from debug logs
        String storedHash = "$2a$10$F2G5TjpTUnEuQ6pKSzkR6uvf7roP0eJA4ea.Niq5lxa96EirR8GTS";
        
        // The password from debug logs
        String providedPassword = "SecurePass123!";
        
        // Test the match
        boolean matches = encoder.matches(providedPassword, storedHash);
        
        System.out.println("Input password: '" + providedPassword + "'");
        System.out.println("Stored hash: '" + storedHash + "'");
        System.out.println("Password match result: " + matches);
        
        // If it doesn't match, try generating a new hash for the provided password to see what it should look like
        if (!matches) {
            String newHash = encoder.encode(providedPassword);
            System.out.println("\nGenerated new hash for '" + providedPassword + "':");
            System.out.println("New hash: " + newHash);
            
            // Also try with some slight variations
            System.out.println("\nTrying with slight variations:");
            checkVariation(encoder, storedHash, providedPassword.trim(), "Trimmed password");
            checkVariation(encoder, storedHash, providedPassword + " ", "Password with trailing space");
            checkVariation(encoder, storedHash, " " + providedPassword, "Password with leading space");
        }
    }
    
    private static void checkVariation(BCryptPasswordEncoder encoder, String storedHash, String variation, String description) {
        boolean matches = encoder.matches(variation, storedHash);
        System.out.println(description + ": '" + variation + "' -> " + matches);
    }
}
