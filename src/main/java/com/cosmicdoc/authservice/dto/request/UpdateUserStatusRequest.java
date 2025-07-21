package com.cosmicdoc.authservice.dto.request;

import com.cosmicdoc.common.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "Status cannot be null.")
    private UserStatus status; // e.g., ACTIVE, SUSPENDED
}