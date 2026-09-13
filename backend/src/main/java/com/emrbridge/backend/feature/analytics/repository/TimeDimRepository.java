package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.TimeDim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeDimRepository extends JpaRepository<TimeDim, Integer> {

    Optional<TimeDim> findByYearMonth(String yearMonth);
}
