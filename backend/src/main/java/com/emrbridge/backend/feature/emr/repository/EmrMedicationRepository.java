package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrMedication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrMedicationRepository extends JpaRepository<EmrMedication, Long> {}
