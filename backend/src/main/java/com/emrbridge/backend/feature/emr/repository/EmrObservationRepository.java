package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrObservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrObservationRepository extends JpaRepository<EmrObservation, Long> {}
