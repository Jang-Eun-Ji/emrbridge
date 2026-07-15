package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrImmunization;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmrImmunizationRepository extends JpaRepository<EmrImmunization, Long> {
    Optional<EmrImmunization> findByPatientAndEncounterAndCodeAndImmDateAndBaseCost(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime immDate, BigDecimal baseCost);

    List<EmrImmunization> findAllByPatientAndEncounterAndCodeAndImmDateAndBaseCost(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime immDate, BigDecimal baseCost);
}
