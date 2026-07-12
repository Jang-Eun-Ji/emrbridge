package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrImmunization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrImmunizationRepository extends JpaRepository<EmrImmunization, Long> {}
