package com.cosmicdoc.authservice.util;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Configuration
public class TestUserCreator {

    private static final String USERS_COLLECTION = "users";
    private static final String ORG_COLLECTION = "organizations";
    private static final String ORG_MEMBERS_COLLECTION = "organization_members";
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Bean
    CommandLineRunner createTestUsers() {
        return args -> {
            System.out.println("Creating test users...");
            
            // Skip Firebase operations if Firebase is disabled
            if (!firebaseEnabled) {
                System.out.println("Firebase is disabled. Skipping test user creation.");
                System.out.println("Test users for development mode:");
                System.out.println("Email: admin@cosmicmed.com");
                System.out.println("Password: Test123!");
                return;
            }
            
            try {
                // Get Firestore instance
                Firestore firestore = FirestoreClient.getFirestore();
                String email = "admin@cosmicmed.com";
                String password = "Test123!";
                String encodedPassword = passwordEncoder.encode(password);
                
                // Check if user already exists
                QuerySnapshot existingUserQuery = firestore.collection(USERS_COLLECTION)
                        .whereEqualTo("email", email)
                        .get()
                        .get();
                        
                String userId;
                String orgId = UUID.randomUUID().toString();
                
                if (!existingUserQuery.isEmpty()) {
                    // Update existing user's password hash
                    System.out.println("Test user already exists, updating password hash...");
                    DocumentSnapshot userDoc = existingUserQuery.getDocuments().get(0);
                    userId = userDoc.getString("id");
                    userDoc.getReference().update("password_hash", encodedPassword).get();
                } else {
                    // Create new user
                    System.out.println("Creating new test user...");
                    userId = UUID.randomUUID().toString();
                    
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", userId);
                    userData.put("email", email);
                    userData.put("name", "Admin User");
                    userData.put("password_hash", encodedPassword);
                    userData.put("status", "ACTIVE");
                    userData.put("created_at", System.currentTimeMillis());
                    
                    firestore.collection(USERS_COLLECTION)
                            .document(userId)
                            .set(userData)
                            .get();
                            
                    // Create test organization
                    Map<String, Object> orgData = new HashMap<>();
                    orgData.put("id", orgId);
                    orgData.put("name", "CosmicMed Test Org");
                    orgData.put("created_at", System.currentTimeMillis());
                    
                    firestore.collection(ORG_COLLECTION)
                            .document(orgId)
                            .set(orgData)
                            .get();
                            
                    // Check if user already has organization membership
                    QuerySnapshot membershipQuery = firestore.collection(ORG_MEMBERS_COLLECTION)
                            .whereEqualTo("userId", userId)
                            .get()
                            .get();
                            
                    if (membershipQuery.isEmpty()) {
                        // Create organization membership
                        Map<String, Object> memberData = new HashMap<>();
                        memberData.put("userId", userId);
                        memberData.put("organizationId", orgId);
                        memberData.put("role", "ROLE_SUPER_ADMIN");
                        memberData.put("status", "ACTIVE");
                        
                        firestore.collection(ORG_MEMBERS_COLLECTION)
                                .document(UUID.randomUUID().toString())
                                .set(memberData)
                                .get();
                    }
                }
                
                System.out.println("Test user setup complete!");
                System.out.println("Email: " + email);
                System.out.println("Password: " + password);
                System.out.println("Password hash: " + encodedPassword);
            } catch (Exception e) {
                System.err.println("Error creating test user: " + e.getMessage());
                e.printStackTrace();
                
                // Print test user info even if Firebase fails
                System.out.println("Default test user (may not be created in Firestore due to error):");
                System.out.println("Email: admin@cosmicmed.com");
                System.out.println("Password: Test123!");
            }
        };
    }
}
