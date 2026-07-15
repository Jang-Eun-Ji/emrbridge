package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrAllergy;
import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmrAllergyRepository extends JpaRepository<EmrAllergy, Long> {
    Optional<EmrAllergy> findByPatientAndEncounterAndCode(
            EmrPatient patient, EmrEncounter encounter, String code);
}
