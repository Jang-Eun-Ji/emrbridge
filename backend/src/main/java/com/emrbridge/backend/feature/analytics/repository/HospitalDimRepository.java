package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.HospitalDim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalDimRepository extends JpaRepository<HospitalDim, Integer> {

    Optional<HospitalDim> findByOriginalOrganizationId(String originalOrganizationId);
}
