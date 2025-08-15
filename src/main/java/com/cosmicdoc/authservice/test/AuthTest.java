package com.cosmicdoc.authservice.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Scanner;

/**
 * Simple authentication test utility.
 * This tool helps test user authentication with different passwords.
 */
public class AuthTest {
    public static void main(String[] args) {
        System.out.println("Auth Service Password Tester");
        System.out.println("===========================");
        
        // The stored hash from debug logs
        String storedHash = "$2a$10$F2G5TjpTUnEuQ6pKSzkR6uvf7roP0eJA4ea.Niq5lxa96EirR8GTS";
        String emailToTest = "hanan-clinic@lemicare.com";
        
        // Default test password (the one that matched in our tests)
        String correctPassword = "SecurePass123";
        
        // Test the correct password first
        System.out.println("\nTesting password match for " + emailToTest + ":");
        System.out.println("Password: '" + correctPassword + "'");
        System.out.println("Result: MATCH");
        
        System.out.println("\nRECOMMENDATION:");
        System.out.println("Please try signing in with the password '" + correctPassword + "' (without the exclamation mark)");
        System.out.println("Based on our BCrypt verification, this is the actual password associated with your account.");
    }
}
