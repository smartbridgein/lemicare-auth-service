package com.cosmicdoc.authservice.dto.response;

import com.cosmicdoc.common.model.UserStatus;
import com.cosmicdoc.common.model.Users;
import com.cosmicdoc.common.model.OrganizationMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private String userId;
    private String email;
    private String displayName;
    private UserStatus status;
    private String mobileNumber;
    private String role; // Role within the specific organization

    /**
     * A factory method to easily create this DTO from the domain models.
     */
    public static UserDetailResponse from(Users user, OrganizationMember membership) {
        return UserDetailResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .mobileNumber(user.getMobileNumber())
                .role(membership.getRole())
                .build();
    }
}