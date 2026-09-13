package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.VisitFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface VisitFactRepository extends JpaRepository<VisitFact, Integer> {

    @Modifying
    @Transactional(transactionManager = "analyticsTransactionManager")
    @Query(value = "TRUNCATE visit_facts, patient_dim, disease_dim, region_dim, time_dim RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateAll();
}
