package com.cosmicdoc.authservice.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Standalone utility to directly update user passwords in Firestore.
 * For development/troubleshooting use only.
 */
public class FirestorePasswordUpdater {

    public static void main(String[] args) {
        try {
            // Initialize Firestore with service account
            String serviceAccountPath = "src/main/resources/service-account.json";
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath));
            FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                    .setCredentials(credentials)
                    .build();
            Firestore firestore = firestoreOptions.getService();
            
            // Initialize password encoder
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            
            // Ask for user email
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter user email to reset password: ");
            String email = scanner.nextLine().trim();
            
            // Find the user by email
            var query = firestore.collection("users").whereEqualTo("email", email);
            var querySnapshot = query.get().get();
            
            if (querySnapshot.isEmpty()) {
                System.out.println("User not found with email: " + email);
                return;
            }
            
            // Get user details
            QueryDocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
            String userId = userDoc.getId();
            
            // Ask for new password
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();
            
            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);
            
            // Update password in Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("hashedPassword", hashedPassword);
            
            firestore.collection("users").document(userId).update(updates).get();
            System.out.println("Password successfully updated for user: " + email);
            
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
