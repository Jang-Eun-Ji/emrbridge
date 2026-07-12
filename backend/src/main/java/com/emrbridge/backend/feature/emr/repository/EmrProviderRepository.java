package com.emrbridge.backend.feature.emr.repository;


import com.emrbridge.backend.feature.emr.domain.EmrProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmrProviderRepository extends JpaRepository<EmrProvider, Long> {
    Optional<EmrProvider> findBySyntheaId(String syntheaId);
    boolean existsBySyntheaId(String syntheaId);
}
