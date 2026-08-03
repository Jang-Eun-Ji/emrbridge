package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrCareplan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmrCareplanRepository extends JpaRepository<EmrCareplan, Long> {
    Optional<EmrCareplan> findBySyntheaId(String syntheaId);
    boolean existsBySyntheaId(String syntheaId);

    @Query("SELECT c FROM EmrCareplan c JOIN FETCH c.patient LEFT JOIN FETCH c.encounter")
    List<EmrCareplan> findAllWithPatientAndEncounter();
}
