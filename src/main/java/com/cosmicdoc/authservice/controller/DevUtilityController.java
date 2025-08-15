package com.cosmicdoc.authservice.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Development utilities controller for direct database operations.
 * IMPORTANT: This controller should ONLY be used for development/debugging.
 * It intentionally bypasses normal security measures and validation.
 */
@RestController
@RequestMapping("/api/public/dev")
public class DevUtilityController {

    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DevUtilityController(Firestore firestore, PasswordEncoder passwordEncoder) {
        this.firestore = firestore;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Direct password reset endpoint for development and troubleshooting.
     * No security or validation - DO NOT USE IN PRODUCTION!
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword) {
        
        try {
            // Find the user by email
            Query query = firestore.collection("users").whereEqualTo("email", email);
            
            // Execute the query
            var querySnapshot = query.get().get();
            
            if (querySnapshot.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found with email: " + email);
            }
            
            // Get the user document
            QueryDocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
            String userId = userDoc.getId();
            
            System.out.println("Found user with ID: " + userId);
            
            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);
            
            // Update just the password field
            Map<String, Object> updates = new HashMap<>();
            updates.put("hashedPassword", hashedPassword);
            
            // Apply the update
            firestore.collection("users").document(userId).update(updates).get();
            
            return ResponseEntity.ok("Password reset successful for user: " + email);
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
