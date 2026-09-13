package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.RegionDim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionDimRepository extends JpaRepository<RegionDim, Integer> {

    Optional<RegionDim> findByRegionName(String regionName);
}
