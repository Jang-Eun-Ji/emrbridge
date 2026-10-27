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
    @Query(value = "TRUNCATE visit_facts, patient_dim, disease_dim, region_dim, time_dim, hospital_dim RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateAll();

    @Query("""
            SELECT t.yearMonth, t.year, t.month, COUNT(v), COUNT(DISTINCT p.patientId)
            FROM VisitFact v
            JOIN v.time t
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            GROUP BY t.timeId, t.yearMonth, t.year, t.month
            ORDER BY t.yearMonth
            """)
    List<Object[]> findMonthlyVisitSummary(@Param("orgId") String orgId);

    @Query("""
            SELECT p.ageGroup, p.gender, COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            GROUP BY p.ageGroup, p.gender
            ORDER BY p.ageGroup, p.gender
            """)
    List<Object[]> findDemographicSummary(@Param("orgId") String orgId);

    @Query("""
            SELECT d.diseaseCode, d.diseaseName, d.diseaseCategory,
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.disease d
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            GROUP BY d.diseaseId, d.diseaseCode, d.diseaseName, d.diseaseCategory
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findDiseaseDistribution(@Param("orgId") String orgId, Pageable pageable);

    @Query("""
            SELECT d.diseaseCode, d.diseaseName, d.diseaseCategory,
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.disease d
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            GROUP BY d.diseaseId, d.diseaseCode, d.diseaseName, d.diseaseCategory
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findDiseaseDistribution(@Param("orgId") String orgId);

    @Query("""
            SELECT r.state, r.city, r.regionName,
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.region r
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            GROUP BY r.regionId, r.state, r.city, r.regionName
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findRegionDistribution(@Param("orgId") String orgId, Pageable pageable);

    @Query("""
            SELECT COALESCE(d.diseaseCategory, 'Other'),
                   COUNT(DISTINCT p.patientId), COALESCE(SUM(v.visitCount), 0)
            FROM VisitFact v
            JOIN v.disease d
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            GROUP BY COALESCE(d.diseaseCategory, 'Other')
            ORDER BY SUM(v.visitCount) DESC
            """)
    List<Object[]> findCategoryDistribution(@Param("orgId") String orgId);

    @Query("""
            SELECT v FROM VisitFact v
            JOIN FETCH v.disease
            LEFT JOIN v.hospital h
            WHERE v.patient.patientId = :patientId
              AND (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            ORDER BY v.visitDate DESC, v.visitId DESC
            """)
    List<VisitFact> findByPatientIdWithDisease(
            @Param("patientId") Integer patientId,
            @Param("orgId") String orgId);

    @Query("""
            SELECT COALESCE(SUM(v.visitCount), 0) FROM VisitFact v
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            """)
    long sumVisitCount(@Param("orgId") String orgId);

    @Query("""
            SELECT COUNT(DISTINCT p.patientId) FROM VisitFact v
            JOIN v.patient p
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            """)
    long countDistinctPatients(@Param("orgId") String orgId);

    @Query("""
            SELECT COUNT(DISTINCT d.diseaseId) FROM VisitFact v
            JOIN v.disease d
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            """)
    long countDistinctDiseases(@Param("orgId") String orgId);

    @Query("""
            SELECT COUNT(DISTINCT r.regionId) FROM VisitFact v
            JOIN v.region r
            LEFT JOIN v.hospital h
            WHERE (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            """)
    long countDistinctRegions(@Param("orgId") String orgId);

    @Query("""
            SELECT COUNT(v) FROM VisitFact v
            LEFT JOIN v.hospital h
            WHERE v.patient.patientId = :patientId
              AND (:orgId IS NULL OR h.originalOrganizationId = :orgId)
            """)
    long countByPatientId(
            @Param("patientId") Integer patientId,
            @Param("orgId") String orgId);
}
