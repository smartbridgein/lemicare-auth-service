package com.cosmicdoc.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    // The 'from' address should be configured, not hardcoded.
    private final String fromAddress = "no-reply@cosmicdoc.com";
    
    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());

    /**
     * Sends the account verification email to a new user.
     * The @Async annotation makes this method execute in a separate thread,
     * so the user's signup request doesn't have to wait for the email to be sent.
     *
     * @param toEmail The recipient's email address.
     * @param token The verification token.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify Your Account for CosmicDoc";
        // In a real app, the frontend URL would come from configuration.
        String verificationUrl = "https://your-frontend.com/verify-account?token=" + token;
        String body = "Welcome to CosmicDoc! Please click the link below to verify your account and set your password:\n" + verificationUrl;

        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends the password reset email.
     * Also runs asynchronously.
     *
     * @param toEmail The recipient's email address.
     * @param token The password reset token.
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Password Reset Request for CosmicDoc";
        String resetUrl = "https://your-frontend.com/reset-password?token=" + token;
        String body = "You requested a password reset. Please click the link below to set a new password:\n" + resetUrl;

        sendEmail(toEmail, subject, body);
    }

    /**
     * A private helper method to construct and send a simple text email.
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            // In a real app, you would have better logging.
            logger.info("Email sent successfully to " + to);
        } catch (Exception e) {
            // This MUST be logged properly. If emails fail to send, it's a critical issue.
            logger.severe("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
    
    /**
     * Sends SMS with the given message to the specified mobile number
     * In a real application, this would use an SMS gateway service
     * 
     * @param mobile The recipient's mobile number
     * @param message The message to send
     */
    @Async
    public void sendSms(String mobile, String message) {
        // In a real application, this would integrate with an SMS gateway service like Twilio
        // For development, just log the message
        logger.info("SMS TO " + mobile + ": " + message);
        
        // Simulate SMS sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}