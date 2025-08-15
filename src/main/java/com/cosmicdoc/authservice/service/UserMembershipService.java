package com.cosmicdoc.authservice.service;

import com.cosmicdoc.common.model.OrganizationMember;
import com.cosmicdoc.common.model.Users;
import com.cosmicdoc.common.repository.OrganizationMemberRepository;
import com.cosmicdoc.common.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Service class for managing user membership operations.
 */
@Service
@RequiredArgsConstructor
public class UserMembershipService {

    private final OrganizationMemberRepository memberRepository;
    private final UsersRepository userRepository;

    /**
     * Gets the primary membership of a user.
     *
     * @param userId The ID of the user
     * @return Optional containing the membership if found, empty otherwise
     */
    public Optional<OrganizationMember> getPrimaryMembership(String userId) {
        // First get the user to find their primary organization
        Optional<Users> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty() || userOptional.get().getOrganizations() == null || userOptional.get().getOrganizations().isEmpty()) {
            return Optional.empty();
        }
        
        // Use the first organization as primary
        String primaryOrgId = userOptional.get().getOrganizations().get(0);
        
        // Now query for the specific membership
        return memberRepository.findByUserIdAndOrgId(userId, primaryOrgId);
    }
}
