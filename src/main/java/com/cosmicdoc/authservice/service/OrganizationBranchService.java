package com.cosmicdoc.authservice.service;

import com.cosmicdoc.authservice.dto.request.BranchRequest;
import com.cosmicdoc.authservice.exception.ResourceNotFoundException;
import com.cosmicdoc.common.model.Branch;
import com.cosmicdoc.common.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationBranchService {

    private final BranchRepository branchRepository;

    public List<Branch> getBranches(String organizationId) {
        return branchRepository.findAllByOrganizationId(organizationId);
    }

    public Branch createBranch(String organizationId, BranchRequest request) {
        String branchId = "branch_" + UUID.randomUUID().toString();

        Branch newBranch = Branch.builder()
                .branchId(branchId)
                .name(request.getName())
                .address(request.getAddress())
                .build();

        return branchRepository.save(organizationId, newBranch);
    }

    public Branch updateBranch(String organizationId, String branchId, BranchRequest request) {
        // First, ensure the branch exists within the given organization before updating.
        Branch existingBranch = branchRepository.findById(organizationId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch with ID " + branchId + " not found in this organization."));

        // Update the fields from the request.
        existingBranch.setName(request.getName());
        existingBranch.setAddress(request.getAddress());

        // Save the updated branch object, overwriting the old one.
        return branchRepository.save(organizationId, existingBranch);
    }
}