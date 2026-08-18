package com.emrbridge.backend.feature.analytics.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
public class PatientDimRepository {

    private final JdbcTemplate jdbcTemplate;

    public PatientDimRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int upsert(
            long originalPatientId,
            String firstName,
            String lastName,
            String gender,
            LocalDate birthDate,
            int age,
            String ageGroup,
            int regionId
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO patient_dim
                    (original_patient_id, first_name, last_name, gender, birth_date, age, age_group, region_id, registration_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (original_patient_id) DO UPDATE
                    SET first_name = EXCLUDED.first_name,
                        last_name = EXCLUDED.last_name,
                        gender = EXCLUDED.gender,
                        birth_date = EXCLUDED.birth_date,
                        age = EXCLUDED.age,
                        age_group = EXCLUDED.age_group,
                        region_id = EXCLUDED.region_id
                RETURNING patient_id
                """,
                Integer.class,
                originalPatientId,
                firstName,
                lastName,
                gender,
                Date.valueOf(birthDate),
                age,
                ageGroup,
                regionId,
                Date.valueOf(birthDate)
        );
    }
}
