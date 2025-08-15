package com.cosmicdoc.authservice.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service class for generating and validating tokens for various purposes.
 */
@Service
public class TokenService {
    
    /**
     * Generates a random token for verification or password reset.
     * 
     * @return A random UUID as a string
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
