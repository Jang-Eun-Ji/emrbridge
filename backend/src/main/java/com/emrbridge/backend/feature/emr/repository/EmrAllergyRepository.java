package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrAllergy;
import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmrAllergyRepository extends JpaRepository<EmrAllergy, Long> {
    Optional<EmrAllergy> findByPatientAndEncounterAndCode(
            EmrPatient patient, EmrEncounter encounter, String code);
    List<EmrAllergy> findAllByPatientAndEncounterAndCode(
            EmrPatient patient, EmrEncounter encounter, String code);

    @Query("SELECT a FROM EmrAllergy a JOIN FETCH a.patient LEFT JOIN FETCH a.encounter")
    List<EmrAllergy> findAllWithPatientAndEncounter();
}
