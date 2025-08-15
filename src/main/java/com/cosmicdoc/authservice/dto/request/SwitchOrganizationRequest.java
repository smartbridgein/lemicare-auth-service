package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the request body of the 'switch-organization' endpoint.
 * Captures the ID of the organization the user wishes to switch their session to.
 */
@Data
@NoArgsConstructor
public class SwitchOrganizationRequest {
      private String targetOrganizationId;
}