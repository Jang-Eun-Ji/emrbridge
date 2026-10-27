package com.emrbridge.backend.feature.auth.security;

import com.emrbridge.backend.feature.auth.domain.AppUser;
import com.emrbridge.backend.feature.auth.domain.AppUserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final AppUserRole role;
    private final String organizationSyntheaId;
    private final String hospitalName;
    private final boolean enabled;

    public AuthUserPrincipal(AppUser user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                user.getOrganizationSyntheaId(),
                user.getHospitalName(),
                user.isEnabled()
        );
    }

    public AuthUserPrincipal(
            Long id,
            String username,
            String passwordHash,
            AppUserRole role,
            String organizationSyntheaId,
            String hospitalName,
            boolean enabled
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.organizationSyntheaId = organizationSyntheaId;
        this.hospitalName = hospitalName;
        this.enabled = enabled;
    }

    public boolean isAdmin() {
        return role == AppUserRole.ADMIN;
    }

    /** Null for admin (no hospital filter); org UUID for hospital users. */
    public String hospitalFilterOrgId() {
        return isAdmin() ? null : organizationSyntheaId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
