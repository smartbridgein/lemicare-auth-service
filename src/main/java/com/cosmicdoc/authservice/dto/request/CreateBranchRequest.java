package com.cosmicdoc.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBranchRequest {
    @NotBlank(message = "Branch name cannot be blank")
    private String name;

    @NotBlank(message = "Branch address cannot be blank")
    private String address;
}