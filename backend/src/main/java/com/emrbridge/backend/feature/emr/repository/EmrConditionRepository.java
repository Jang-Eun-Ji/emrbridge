package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrCondition;
import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface EmrConditionRepository extends JpaRepository<EmrCondition, Long> {
    Optional<EmrCondition> findByPatientAndEncounterAndCodeAndStartDate(
            EmrPatient patient, EmrEncounter encounter, String code, LocalDate startDate);

}
