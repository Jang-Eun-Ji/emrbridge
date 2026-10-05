package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.VisitFact;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VisitFactRepository extends JpaRepository<VisitFact, Integer> {

    @Modifying
    @Transactional(transactionManager = "analyticsTransactionManager")
    @Query(value = "TRUNCATE visit_facts, patient_dim, disease_dim, region_dim, time_dim RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateAll();

    @Query("""
            SELECT t.yearMonth, t.year, t.month, COUNT(v), COUNT(DISTINCT p.patientId)
            FROM VisitFact v
            JOIN v.time t
            JOIN v.patient p
            GROUP BY t.timeId, t.yearMonth, t.year, t.month
            ORDER BY t.yearMonth
            """)
    List<Object[]> findMonthlyVisitSummary();

    @Query("""
            SELECT p.ageGroup, p.gender, COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.patient p
            GROUP BY p.ageGroup, p.gender
            ORDER BY p.ageGroup, p.gender
            """)
    List<Object[]> findDemographicSummary();

    @Query("""
            SELECT d.diseaseCode, d.diseaseName, d.diseaseCategory,
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.disease d
            JOIN v.patient p
            GROUP BY d.diseaseId, d.diseaseCode, d.diseaseName, d.diseaseCategory
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findDiseaseDistribution(Pageable pageable);

    @Query("""
            SELECT d.diseaseCode, d.diseaseName, d.diseaseCategory,
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.disease d
            JOIN v.patient p
            GROUP BY d.diseaseId, d.diseaseCode, d.diseaseName, d.diseaseCategory
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findDiseaseDistribution();

    @Query("""
            SELECT r.state, r.city, r.regionName,
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.region r
            JOIN v.patient p
            GROUP BY r.regionId, r.state, r.city, r.regionName
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findRegionDistribution(Pageable pageable);

    @Query("""
            SELECT COALESCE(d.diseaseCategory, '기타'),
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.disease d
            JOIN v.patient p
            GROUP BY COALESCE(d.diseaseCategory, '기타')
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findCategoryDistribution();

    @Query("""
            SELECT v FROM VisitFact v
            JOIN FETCH v.disease
            WHERE v.patient.patientId = :patientId
            ORDER BY v.visitDate DESC, v.visitId DESC
            """)
    List<VisitFact> findByPatientIdWithDisease(@Param("patientId") Integer patientId);

    @Query("SELECT COALESCE(SUM(v.visitCount), 0) FROM VisitFact v")
    long sumVisitCount();
}
