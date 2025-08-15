package com.cosmicdoc.authservice.controller;

import com.cosmicdoc.authservice.dto.request.OtpRequest;
import com.cosmicdoc.authservice.service.OtpService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
public class OtpController {

    private final OtpService otpService;

    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@Valid @RequestBody OtpRequest request) {
        try {
            otpService.sendOtp(request.getMobile(), request.getPurpose());
            return ResponseEntity.ok()
                .body(new ApiResponse(true, "OTP sent successfully to " + request.getMobile()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String message;
        
        public static ApiResponse success(String message) {
            return new ApiResponse(true, message);
        }
        
        public static ApiResponse error(String message) {
            return new ApiResponse(false, message);
        }
        
        public String getMessage() {
            return message;
        }
    }
}
