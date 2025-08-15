package com.cosmicdoc.authservice.test;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Standalone utility for resetting user passwords directly in Firestore
 * 
 * Usage: java -cp target/classes:target/dependency/* com.cosmicdoc.authservice.test.PasswordResetUtility
 */
public class PasswordResetUtility {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("===== Password Reset Utility =====");
        System.out.println("This utility will reset a user's password in Firestore");

        System.out.println("\nEnter path to service account JSON file:");
        String serviceAccountPath = scanner.nextLine().trim();
        
        System.out.println("Enter user email:");
        String email = scanner.nextLine().trim();
        
        System.out.println("Enter new password:");
        String newPassword = scanner.nextLine().trim();

        try {
            // Initialize Firebase with service account
            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            Firestore firestore = FirestoreClient.getFirestore();
            
            // Query for user by email
            var query = firestore.collection("users").whereEqualTo("email", email).get().get();
            
            if (query.isEmpty()) {
                System.out.println("Error: No user found with email: " + email);
                return;
            }
            
            // Get the user document
            DocumentSnapshot userDoc = query.getDocuments().get(0);
            String userId = userDoc.getId();
            
            System.out.println("Found user with ID: " + userId);
            
            // Hash the new password using BCrypt
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hashedPassword = encoder.encode(newPassword);
            
            // Update just the password field
            DocumentReference userRef = firestore.collection("users").document(userId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("hashedPassword", hashedPassword);
            
            // Apply the update
            userRef.update(updates).get();
            
            System.out.println("Password successfully reset for user: " + email);
            System.out.println("New password hash: " + hashedPassword);

        } catch (IOException | ExecutionException | InterruptedException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
