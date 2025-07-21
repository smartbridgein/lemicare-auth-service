package com.cosmicdoc.authservice.util;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Utility class for resetting user passwords directly in Firestore.
 * For development use only - NOT for production environments!
 */
@Component
@Profile("dev") // Only activate in dev environment
public class PasswordResetUtility implements CommandLineRunner {

    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetUtility(Firestore firestore, PasswordEncoder passwordEncoder) {
        this.firestore = firestore;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // This method will run automatically on startup if the profile is set to "dev"
        System.out.println("Password Reset Utility is available in DEV mode.");
        System.out.println("To reset a password, call resetUserPassword(email, newPassword)");
    }

    /**
     * Resets a user's password directly in Firestore.
     * @param email The email of the user whose password is to be reset
     * @param newPassword The new password (will be hashed before storage)
     * @return true if successful, false otherwise
     */
    public boolean resetUserPassword(String email, String newPassword) {
        try {
            // Find the user by email
            Query query = firestore.collection("users").whereEqualTo("email", email);
            
            // Execute the query
            var querySnapshot = query.get().get();
            
            if (querySnapshot.isEmpty()) {
                System.out.println("User not found with email: " + email);
                return false;
            }
            
            // Get the user document
            QueryDocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
            String userId = userDoc.getId();
            
            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);
            
            // Update just the password field
            Map<String, Object> updates = new HashMap<>();
            updates.put("hashedPassword", hashedPassword);
            
            // Apply the update
            firestore.collection("users").document(userId).update(updates).get();
            
            System.out.println("Password successfully reset for user: " + email);
            return true;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
