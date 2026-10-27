package com.emrbridge.backend.feature.auth.repository;

import com.emrbridge.backend.feature.auth.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByOrganizationSyntheaId(String organizationSyntheaId);
}
