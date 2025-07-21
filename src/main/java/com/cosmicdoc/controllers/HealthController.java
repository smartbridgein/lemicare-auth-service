package com.cosmicdoc.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

/**
 * Ultra minimal health controller for Cloud Run deployment
 * All endpoints are stateless and don't require any dependencies
 */
@RestController
public class HealthController {

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String root() {
        // Absolute minimal endpoint
        return "OK";
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        // Simple health check that will definitely respond
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"status\":\"UP\"}");
    }

    // Additional endpoint for GCP health checks
    @GetMapping("/_ah/health")
    public ResponseEntity<String> gcpHealth() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"status\":\"UP\"}");
    }
}
