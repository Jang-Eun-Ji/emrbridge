package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import com.emrbridge.backend.feature.emr.domain.EmrProcedure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmrProcedureRepository extends JpaRepository<EmrProcedure, Long> {
    Optional<EmrProcedure> findByPatientAndEncounterAndCodeAndProcDateAndBaseCost(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime procDate, BigDecimal baseCost);

    List<EmrProcedure> findAllByPatientAndEncounterAndCodeAndProcDateAndBaseCost(
            EmrPatient patient, EmrEncounter encounter, String code,
            LocalDateTime procDate, BigDecimal baseCost);
}
