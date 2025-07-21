package com.cosmicdoc.authservice.controller;

import com.cosmicdoc.authservice.dto.request.BranchRequest;
import com.cosmicdoc.authservice.exception.ResourceNotFoundException;
import com.cosmicdoc.authservice.security.SecurityUtils;
import com.cosmicdoc.authservice.service.OrganizationBranchService;
import com.cosmicdoc.common.model.Branch;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')") // Secures all endpoints in this controller
public class OrganizationBranchController {

    private final OrganizationBranchService branchService;

    /**
     * Endpoint to create a new branch within the authenticated admin's organization.
     */
    @PostMapping("/")
    public ResponseEntity<Branch> createBranch(@Valid @RequestBody BranchRequest request) {
        String organizationId = SecurityUtils.getOrganizationId();
        Branch createdBranch = branchService.createBranch(organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
    }

    /**
     * Endpoint to update an existing branch within the authenticated admin's organization.
     */
    @PutMapping("/{branchId}")
    public ResponseEntity<?> updateBranch(
            @PathVariable String branchId,
            @Valid @RequestBody BranchRequest request) {
        try {
            String organizationId = SecurityUtils.getOrganizationId();
            Branch updatedBranch = branchService.updateBranch(organizationId, branchId, request);
            return ResponseEntity.ok(updatedBranch);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    /**
     * Endpoint to list all branches for the authenticated admin's organization.
     */
    @GetMapping("/")
    public ResponseEntity<List<Branch>> listBranches() {
        String organizationId = SecurityUtils.getOrganizationId();
        List<Branch> branches = branchService.getBranches(organizationId);
        return ResponseEntity.ok(branches);
    }
}