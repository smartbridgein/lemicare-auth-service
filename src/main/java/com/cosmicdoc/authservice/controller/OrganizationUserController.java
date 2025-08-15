package com.cosmicdoc.authservice.controller;

import com.cosmicdoc.authservice.dto.request.CreateUserRequest;
import com.cosmicdoc.authservice.dto.request.UpdateUserRequest;
import com.cosmicdoc.authservice.dto.request.UpdateUserStatusRequest;
import com.cosmicdoc.authservice.dto.response.UserDetailResponse;
import com.cosmicdoc.authservice.exception.ResourceNotFoundException;
import com.cosmicdoc.authservice.security.SecurityUtils; // A helper class to get context from JWT
import com.cosmicdoc.authservice.service.OrganizationUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')") // Secures all methods in this controller
public class OrganizationUserController {

    private final OrganizationUserService organizationUserService;

    /**
     * Endpoint for an admin to create a new user within their organization.
     */
    @PostMapping("/")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            // Get the admin's organization ID from their JWT token to ensure
            // they can only create users in their own organization.
            String adminOrgId = SecurityUtils.getOrganizationId();

            organizationUserService.createUserInOrg(adminOrgId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully. An invitation has been sent.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    /**
     * Endpoint for an admin to activate or suspend a user in their organization.
     */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        try {
            String adminOrgId = SecurityUtils.getOrganizationId();
            organizationUserService.updateUserStatus(adminOrgId, userId, request);
            return ResponseEntity.ok("User status updated successfully.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    /**
     * Endpoint to list all users within the admin's organization.
     */
    @GetMapping("/")
    public ResponseEntity<List<UserDetailResponse>> listUsers() {
        String adminOrgId = SecurityUtils.getOrganizationId();
        List<UserDetailResponse> users = organizationUserService.getUsersInOrganization(adminOrgId);
        return ResponseEntity.ok(users);
    }

    /**
     * Endpoint to fetch the details of a single user within the admin's organization.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserDetails(@PathVariable String userId) {
        try {
            String adminOrgId = SecurityUtils.getOrganizationId();
            UserDetailResponse userDetail = organizationUserService.getUserInOrganization(adminOrgId, userId);
            return ResponseEntity.ok(userDetail);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint to update a user's details and permissions (role).
     */
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        try {
            String adminOrgId = SecurityUtils.getOrganizationId();
            organizationUserService.updateUserInOrganization(adminOrgId, userId, request);
            return ResponseEntity.ok("User updated successfully.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }
}