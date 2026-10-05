package com.emrbridge.backend.feature.analytics.repository;

import com.emrbridge.backend.feature.analytics.domain.PatientDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientDimRepository extends JpaRepository<PatientDim, Integer> {

    Optional<PatientDim> findByOriginalPatientId(Long originalPatientId);

    @Query("""
            SELECT DISTINCT p FROM PatientDim p
            LEFT JOIN FETCH p.region
            WHERE :q = ''
               OR LOWER(COALESCE(p.firstName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(p.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR CAST(p.patientId AS string) LIKE CONCAT('%', :q, '%')
               OR CAST(p.originalPatientId AS string) LIKE CONCAT('%', :q, '%')
            ORDER BY p.patientId
            """)
    List<PatientDim> search(@Param("q") String q);

    @Query("""
            SELECT p FROM PatientDim p
            LEFT JOIN FETCH p.region
            WHERE p.patientId = :patientId
            """)
    Optional<PatientDim> findByIdWithRegion(@Param("patientId") Integer patientId);
}
