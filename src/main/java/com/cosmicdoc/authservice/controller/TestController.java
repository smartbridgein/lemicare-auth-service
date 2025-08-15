package com.cosmicdoc.authservice.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/public/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final Firestore firestore;
    
    @GetMapping("/activate-user")
    public ResponseEntity<String> activateUser(@RequestParam String email) {
        try {
            // Find user by email
            QuerySnapshot querySnapshot = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .get();
            
            if (querySnapshot.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update user status to ACTIVE
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                String userId = document.getId();
                log.info("Found user with ID: {}", userId);
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "ACTIVE");
                
                firestore.collection("users").document(userId)
                        .update(updates)
                        .get();
                
                return ResponseEntity.ok("User activated successfully with ID: " + userId);
            }
            
            return ResponseEntity.notFound().build();
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error activating user: ", e);
            return ResponseEntity.internalServerError().body("Error activating user: " + e.getMessage());
        }
    }
    
    @GetMapping("/user-tokens")
    public ResponseEntity<List<Map<String, Object>>> getUserTokens(@RequestParam String email) {
        try {
            // Find user by email
            QuerySnapshot userSnapshot = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .get();
            
            if (userSnapshot.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            String userId = userSnapshot.getDocuments().get(0).getId();
            log.info("Found user with ID: {}", userId);
            
            // Find tokens for this user
            List<Map<String, Object>> tokens = new ArrayList<>();
            
            // Check verification tokens
            QuerySnapshot verificationTokens = firestore.collection("verification_tokens")
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            for (QueryDocumentSnapshot doc : verificationTokens.getDocuments()) {
                Map<String, Object> tokenData = new HashMap<>(doc.getData());
                tokenData.put("tokenId", doc.getId());
                tokenData.put("type", "verification");
                tokens.add(tokenData);
            }
            
            // Check password reset tokens
            QuerySnapshot resetTokens = firestore.collection("password_reset_tokens")
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            for (QueryDocumentSnapshot doc : resetTokens.getDocuments()) {
                Map<String, Object> tokenData = new HashMap<>(doc.getData());
                tokenData.put("tokenId", doc.getId());
                tokenData.put("type", "password_reset");
                tokens.add(tokenData);
            }
            
            return ResponseEntity.ok(tokens);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error retrieving tokens: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
