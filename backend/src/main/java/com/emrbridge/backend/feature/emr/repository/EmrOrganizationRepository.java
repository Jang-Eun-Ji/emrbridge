package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmrOrganizationRepository extends JpaRepository<EmrOrganization, Long> {
    Optional<EmrOrganization> findBySyntheaId(String syntheaId);
    boolean existsBySyntheaId(String syntheaId);
}