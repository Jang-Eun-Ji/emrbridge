package com.emrbridge.backend.feature.emr.repository;

import com.emrbridge.backend.feature.emr.domain.EmrProcedure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrProcedureRepository extends JpaRepository<EmrProcedure, Long> {}
