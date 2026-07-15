package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrCareplan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmrCareplanRepository extends JpaRepository<EmrCareplan, Long> {
    Optional<EmrCareplan> findBySyntheaId(String syntheaId);
    boolean existsBySyntheaId(String syntheaId);
}
