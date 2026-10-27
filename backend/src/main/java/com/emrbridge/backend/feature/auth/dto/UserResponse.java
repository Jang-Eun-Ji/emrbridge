package com.emrbridge.backend.feature.auth.dto;

import com.emrbridge.backend.feature.auth.domain.AppUserRole;
import com.emrbridge.backend.feature.auth.security.AuthUserPrincipal;

public record UserResponse(
        Long id,
        String username,
        AppUserRole role,
        String organizationSyntheaId,
        String hospitalName,
        boolean admin
) {
    public static UserResponse from(AuthUserPrincipal principal) {
        return new UserResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getRole(),
                principal.getOrganizationSyntheaId(),
                principal.getHospitalName(),
                principal.isAdmin()
        );
    }
}
