package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmrPatientRepository extends JpaRepository<EmrPatient, Long> {

    Optional<EmrPatient> findBySyntheaPatientId(String syntheaPatientId);

    boolean existsBySyntheaPatientId(String syntheaPatientId);

    // Synthea UUID exists → allows identification
    // EmrOrganizationRepository
    public interface EmrOrganizationRepository extends JpaRepository<EmrOrganization, Long> {
        Optional<EmrOrganization> findBySyntheaId(String syntheaId);
        boolean existsBySyntheaId(String syntheaId);
    }

    // EmrProviderRepository
    public interface EmrProviderRepository extends JpaRepository<EmrProvider, Long> {
        Optional<EmrProvider> findBySyntheaId(String syntheaId);
        boolean existsBySyntheaId(String syntheaId);
    }

    // EmrEncounterRepository
    public interface EmrEncounterRepository extends JpaRepository<EmrEncounter, Long> {
        Optional<EmrEncounter> findBySyntheaId(String syntheaId);
        boolean existsBySyntheaId(String syntheaId);
    }

    // EmrCareplanRepository
    public interface EmrCareplanRepository extends JpaRepository<EmrCareplan, Long> {
        boolean existsBySyntheaId(String syntheaId);
    }


    // no unique ID and no duplicate data -> no need for findBySyntheaId or existsBySyntheaId
    public interface EmrConditionRepository extends JpaRepository<EmrCondition, Long> {}
    public interface EmrObservationRepository extends JpaRepository<EmrObservation, Long> {}
    public interface EmrMedicationRepository extends JpaRepository<EmrMedication, Long> {}
    public interface EmrProcedureRepository extends JpaRepository<EmrProcedure, Long> {}
    public interface EmrAllergyRepository extends JpaRepository<EmrAllergy, Long> {}
    public interface EmrImmunizationRepository extends JpaRepository<EmrImmunization, Long> {}




}