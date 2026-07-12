package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrCareplan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrCareplanRepository extends JpaRepository<EmrCareplan, Long> {
    boolean existsBySyntheaId(String syntheaId);
}
