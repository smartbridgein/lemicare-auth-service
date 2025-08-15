package com.cosmicdoc.authservice.controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple test controller to validate Firebase connectivity in the auth-service
 * This controller doesn't depend on any of the service's business logic
 */
@RestController
@RequestMapping("/test")
public class FirebaseTestController {

    private final FirebaseApp firebaseApp;
    private final Firestore firestore;

    @Autowired
    public FirebaseTestController(FirebaseApp firebaseApp, Firestore firestore) {
        this.firebaseApp = firebaseApp;
        this.firestore = firestore;
    }

    /**
     * Test Firebase connection status
     * @return Connection status information
     */
    @GetMapping("/firebase-status")
    public ResponseEntity<Map<String, Object>> testFirebaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if Firebase is initialized
            boolean firebaseInitialized = firebaseApp != null && !firebaseApp.getName().isEmpty();
            response.put("firebaseInitialized", firebaseInitialized);
            response.put("firebaseAppName", firebaseApp.getName());
            
            // Check if Firestore is connected
            boolean firestoreConnected = firestore != null;
            response.put("firestoreConnected", firestoreConnected);
            
            if (firestoreConnected) {
                // Try to access a collection to verify full connectivity
                firestore.collection("test").document("connectivity").get();
                response.put("firestoreAccessible", true);
                response.put("status", "All Firebase services are properly connected");
            } else {
                response.put("status", "Firebase initialized but Firestore not connected");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "Firebase connection error");
            return ResponseEntity.status(500).body(response);
        }
    }
}
