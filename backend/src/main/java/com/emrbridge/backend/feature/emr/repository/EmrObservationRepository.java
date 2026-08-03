package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrObservation;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT o FROM EmrObservation o JOIN FETCH o.patient LEFT JOIN FETCH o.encounter")
    List<EmrObservation> findAllWithPatientAndEncounter();
}
