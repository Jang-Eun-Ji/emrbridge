package com.emrbridge.backend.feature.analytics.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
public class VisitFactRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitFactRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(
            int patientDimId,
            int diseaseDimId,
            int timeDimId,
            int regionDimId,
            LocalDate visitDate,
            String visitType,
            Long originalEncounterId
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO visit_facts
                    (patient_id, disease_id, time_id, region_id, visit_date, visit_count, visit_type, original_encounter_id)
                VALUES (?, ?, ?, ?, ?, 1, ?, ?)
                """,
                patientDimId,
                diseaseDimId,
                timeDimId,
                regionDimId,
                Date.valueOf(visitDate),
                visitType,
                originalEncounterId
        );
    }
}
