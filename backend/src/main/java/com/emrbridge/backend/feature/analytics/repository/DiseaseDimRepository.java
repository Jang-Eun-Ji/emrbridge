package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.DiseaseDim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseDimRepository extends JpaRepository<DiseaseDim, Integer> {

    Optional<DiseaseDim> findByDiseaseCode(String diseaseCode);
}
