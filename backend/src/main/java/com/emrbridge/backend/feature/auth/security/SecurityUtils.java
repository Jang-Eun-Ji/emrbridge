package com.emrbridge.backend.feature.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthUserPrincipal requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new AccessDeniedException("Authentication required");
        }
        return principal;
    }
}
