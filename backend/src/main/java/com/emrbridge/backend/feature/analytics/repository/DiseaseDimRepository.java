package com.emrbridge.backend.feature.analytics.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DiseaseDimRepository {

    private final JdbcTemplate jdbcTemplate;

    public DiseaseDimRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int upsert(String diseaseCode, String diseaseName, String diseaseCategory) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO disease_dim (disease_code, disease_name, disease_category)
                VALUES (?, ?, ?)
                ON CONFLICT (disease_code) DO UPDATE SET disease_name = EXCLUDED.disease_name
                RETURNING disease_id
                """,
                Integer.class,
                diseaseCode,
                diseaseName,
                diseaseCategory
        );
    }
}
