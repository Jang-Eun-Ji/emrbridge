package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrMedication;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmrMedicationRepository extends JpaRepository<EmrMedication, Long> {
    Optional<EmrMedication> findByPatientAndEncounterAndCodeAndStartTimeAndDispenses(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime startTime, Integer dispenses);

}
