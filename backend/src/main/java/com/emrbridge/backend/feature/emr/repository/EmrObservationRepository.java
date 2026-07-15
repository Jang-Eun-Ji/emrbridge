package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrObservation;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmrObservationRepository extends JpaRepository<EmrObservation, Long> {
    Optional<EmrObservation> findByPatientAndEncounterAndCodeAndObsDateAndValue(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime obsDate, String value);

    List<EmrObservation> findAllByPatientAndEncounterAndCodeAndObsDateAndValue(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime obsDate, String value);
}
