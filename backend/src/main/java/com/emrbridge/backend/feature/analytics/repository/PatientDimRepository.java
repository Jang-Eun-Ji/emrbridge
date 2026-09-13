package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.PatientDim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDimRepository extends JpaRepository<PatientDim, Integer> {

    Optional<PatientDim> findByOriginalPatientId(Long originalPatientId);
}
