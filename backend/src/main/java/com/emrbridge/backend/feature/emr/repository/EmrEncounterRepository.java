package com.emrbridge.backend.feature.emr.repository;


import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmrEncounterRepository extends JpaRepository<EmrEncounter, Long> {
    Optional<EmrEncounter> findBySyntheaId(String syntheaId);
    boolean existsBySyntheaId(String syntheaId);
}
