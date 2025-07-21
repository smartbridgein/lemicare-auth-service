package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobile;
    
    @NotBlank(message = "Purpose is required")
    @Pattern(regexp = "^(login|signup|reset)$", message = "Purpose must be login, signup, or reset")
    private String purpose;
}
