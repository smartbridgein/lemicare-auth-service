package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SuperAdminSignupRequest {
    @NotBlank
    private String organizationName;
    private boolean hasMultipleBranches;
    @NotBlank
    private String initialBranchName;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String mobileNumber;

    private String address;
}