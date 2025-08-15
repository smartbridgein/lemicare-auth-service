package com.cosmicdoc.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/auth")
public class UserSessionController {

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        // In a stateless JWT authentication system, the server doesn't need to invalidate the token
        // Client-side will remove the token, so we just return a success response
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
