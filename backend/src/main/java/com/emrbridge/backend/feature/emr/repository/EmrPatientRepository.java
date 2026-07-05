package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmrPatientRepository extends JpaRepository<EmrPatient, Long> {
    Optional<EmrPatient> findBySyntheaPatientId(String syntheaPatientId);
    boolean existsBySyntheaPatientId(String syntheaPatientId);

}
