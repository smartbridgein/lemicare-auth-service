package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating an organization's branch.
 * Contains all the necessary information provided by the client.
 */
@Data
@NoArgsConstructor
public class BranchRequest {

    @NotBlank(message = "Branch name is required and cannot be blank.")
    private String name;

    @NotBlank(message = "Branch address is required and cannot be blank.")
    private String address;
}