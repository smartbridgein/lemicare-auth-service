package com.cosmicdoc.authservice.service;

import com.cosmicdoc.authservice.dto.request.CreateUserRequest;
import com.cosmicdoc.authservice.dto.request.UpdateUserRequest;
import com.cosmicdoc.authservice.dto.request.UpdateUserStatusRequest;
import com.cosmicdoc.authservice.dto.response.UserDetailResponse;
import com.cosmicdoc.authservice.exception.ResourceNotFoundException;
import com.cosmicdoc.common.model.OrganizationMember;
import com.cosmicdoc.common.model.UserStatus;
import com.cosmicdoc.common.model.Users;
import com.cosmicdoc.common.repository.OrganizationMemberRepository;
import com.cosmicdoc.common.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationUserService {

    private final UsersRepository userRepository;
    private final OrganizationMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user within a specific organization.
     * If a user with the given email already exists, they are added to the organization.
     * If not, a new user account is created in a PENDING_VERIFICATION state.
     *
     * @param adminOrgId The organization ID of the admin performing the action.
     * @param request The user creation request details.
     */
    public void createUserInOrg(String adminOrgId, CreateUserRequest request) {
        // Find if a user with this email already exists globally.
        Optional<Users> existingUserOpt = userRepository.findByEmail(request.getEmail());

        Users userToProcess;
        if (existingUserOpt.isPresent()) {
            // User already exists, we will just add them to the new organization.
            userToProcess = existingUserOpt.get();

            // Check if the user is already a member of this organization.
            if (userToProcess.getOrganizations().contains(adminOrgId)) {
                throw new IllegalStateException("User is already a member of this organization.");
            }

            // Add the new organization to the user's list and update them.
            userToProcess.getOrganizations().add(adminOrgId);
            userRepository.save(userToProcess); // Assuming save handles updates if ID exists.

        } else {
            // User does not exist, create a new user record.
            String userId = "user_" + UUID.randomUUID().toString();
            userToProcess = Users.builder()
                    .userId(userId)
                    .email(request.getEmail())
                    .displayName(request.getDisplayName())
                    .status(UserStatus.PENDING_VERIFICATION) // User must verify and set a password.
                    .hashedPassword(passwordEncoder.encode(UUID.randomUUID().toString())) // Secure temp password
                    .organizations(Collections.singletonList(adminOrgId))
                    .build();
            userRepository.save(userToProcess);
        }

        // Create the membership link between the user and the organization.
        OrganizationMember membership = OrganizationMember.builder()
                .userId(userToProcess.getUserId())
                .organizationId(adminOrgId)
                .role(request.getRole())
                .build();
        memberRepository.save(membership);

        // TODO: Send an invitation/welcome email to the new user.
    }

    /**
     * Updates the status of a user within an organization.
     * @param adminOrgId The organization ID of the admin.
     * @param userIdToUpdate The ID of the user whose status is being changed.
     * @param request The new status.
     */
    public void updateUserStatus(String adminOrgId, String userIdToUpdate, UpdateUserStatusRequest request) throws ResourceNotFoundException {
        // 1. Verify the user to be updated is actually a member of the admin's organization.
        memberRepository.findByUserIdAndOrgId(userIdToUpdate, adminOrgId)
                .orElseThrow(() -> new SecurityException("Target user is not a member of this organization."));

        // 2. Fetch the user's global profile.
        Users user = userRepository.findById(userIdToUpdate)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userIdToUpdate + " not found."));

        // 3. Update the status and save.
        user.setStatus(request.getStatus());
        userRepository.save(user); // save() method should handle updates.
    }

    /**
     * Fetches a list of all users belonging to a specific organization.
     *
     * @param adminOrgId The organization ID of the admin making the request.
     * @return A list of detailed user profiles.
     */
    public List<UserDetailResponse> getUsersInOrganization(String adminOrgId) {
        // 1. Find all memberships for the given organization.
        List<OrganizationMember> memberships = memberRepository.findAllByOrganizationId(adminOrgId);

        // 2. For each membership, fetch the corresponding user's global profile.
        //    This approach can be simplified if the repository supports fetching multiple users by ID.
        return memberships.stream()
                .map(membership -> {
                    Users user = userRepository.findById(membership.getUserId())
                            .orElse(null); // Or handle more gracefully if user doc is missing
                    if (user != null) {
                        return UserDetailResponse.from(user, membership);
                    }
                    return null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
        // TODO: For production, add filtering by role/branch and pagination.
    }

    /**
     * Fetches the detailed profile of a single user within the admin's organization.
     *
     * @param adminOrgId The organization ID of the admin.
     * @param userIdToFetch The ID of the user to fetch.
     * @return The detailed user profile.
     */
    public UserDetailResponse getUserInOrganization(String adminOrgId, String userIdToFetch) throws ResourceNotFoundException {
        // 1. First, verify the user is a member of the admin's organization to ensure authorization.
        OrganizationMember membership = memberRepository.findByUserIdAndOrgId(userIdToFetch, adminOrgId)
                .orElseThrow(() -> new SecurityException("Target user is not a member of this organization."));

        // 2. If they are a member, fetch their global user profile.
        Users user = userRepository.findById(userIdToFetch)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userIdToFetch + " not found."));

        // 3. Combine the information into a single response DTO.
        return UserDetailResponse.from(user, membership);
    }

    /**
     * Updates the details (like name and role) of a user within the admin's organization.
     *
     * @param adminOrgId The organization ID of the admin.
     * @param userIdToUpdate The ID of the user being updated.
     * @param request The request DTO with the new details.
     */
    public void updateUserInOrganization(String adminOrgId, String userIdToUpdate, UpdateUserRequest request) throws ResourceNotFoundException {
        // 1. Verify the user is a member of the organization.
        OrganizationMember membership = memberRepository.findByUserIdAndOrgId(userIdToUpdate, adminOrgId)
                .orElseThrow(() -> new SecurityException("Target user is not a member of this organization."));

        // 2. Fetch the user's global profile.
        Users user = userRepository.findById(userIdToUpdate)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userIdToUpdate + " not found."));

        // 3. Update the fields on the domain objects.
        user.setDisplayName(request.getDisplayName());
        membership.setRole(request.getRole());

        // 4. Save the updated objects back to the database.
        //    This should ideally be a transactional operation if you had a SQL DB.
        //    With Firestore, these are two separate writes.
        userRepository.save(user);
        memberRepository.save(membership);
    }
}
