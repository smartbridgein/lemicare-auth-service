package com.cosmicdoc.authservice.service;

import com.cosmicdoc.common.model.Users;
import com.cosmicdoc.common.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
public class OtpService {
    
    private final UsersRepository usersRepository;
    private final NotificationService notificationService;
    private static final Logger logger = Logger.getLogger(OtpService.class.getName());
    
    // In-memory OTP store (for development purposes)
    // In production, this should be replaced with Redis or another distributed cache
    private final Map<String, OtpData> otpStorage = new HashMap<>();
    
    @Autowired
    public OtpService(UsersRepository usersRepository, NotificationService notificationService) {
        this.usersRepository = usersRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Generates and sends an OTP to the specified mobile number
     *
     * @param mobile  Mobile number
     * @param purpose Purpose of OTP (login, signup, reset)
     * @throws ExecutionException if user not found for reset/login
     * @throws InterruptedException if database operation is interrupted
     */
    public void sendOtp(String mobile, String purpose) throws ExecutionException, InterruptedException {
        // For reset and login, verify user exists
        if ("reset".equals(purpose) || "login".equals(purpose)) {
            Optional<Users> userOptional = usersRepository.findByPhone(mobile);
            if (userOptional.isEmpty()) {
                throw new IllegalArgumentException("No account found with this mobile number");
            }
        }
        
        // Generate a 6-digit OTP
        String otp = generateOtp();
        
        // Store the OTP with expiration time (5 minutes)
        otpStorage.put(mobile, new OtpData(otp, LocalDateTime.now().plusMinutes(5), purpose));
        
        // In a real application, send the OTP via SMS
        logger.info("Sending OTP: " + otp + " to mobile: " + mobile + " for purpose: " + purpose);
        
        // If notification service is available, use it
        try {
            notificationService.sendSms(mobile, "Your verification code is: " + otp);
        } catch (Exception e) {
            logger.warning("Failed to send SMS: " + e.getMessage());
            // Don't throw exception, as we might be in development mode without SMS capability
        }
    }
    
    /**
     * Validates an OTP for the specified mobile and purpose
     *
     * @param mobile  Mobile number
     * @param otp     OTP to validate
     * @param purpose Purpose of OTP
     * @return true if valid, false otherwise
     */
    public boolean validateOtp(String mobile, String otp, String purpose) {
        OtpData otpData = otpStorage.get(mobile);
        
        if (otpData == null) {
            return false;
        }
        
        // Check if OTP is expired
        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpStorage.remove(mobile);
            return false;
        }
        
        // Check if purpose matches
        if (!otpData.getPurpose().equals(purpose)) {
            return false;
        }
        
        // Check if OTP matches
        boolean isValid = otpData.getOtp().equals(otp);
        
        // Remove OTP after successful validation to prevent reuse
        if (isValid) {
            otpStorage.remove(mobile);
        }
        
        return isValid;
    }
    
    /**
     * Generates a random 6-digit OTP
     *
     * @return 6-digit OTP
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000); // 6-digit number between 100000 and 999999
        return String.valueOf(num);
    }
    
    /**
     * OTP data class to store OTP, expiry time, and purpose
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;
        private final String purpose;
        
        public OtpData(String otp, LocalDateTime expiryTime, String purpose) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.purpose = purpose;
        }
        
        public String getOtp() {
            return otp;
        }
        
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
        
        public String getPurpose() {
            return purpose;
        }
    }
}
