package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrConditionRepository extends JpaRepository<EmrCondition, Long> {}
