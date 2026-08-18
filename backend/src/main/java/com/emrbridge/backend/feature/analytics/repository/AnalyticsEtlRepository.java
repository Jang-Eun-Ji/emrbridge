package com.emrbridge.backend.feature.analytics.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsEtlRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsEtlRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void truncateAll() {
        jdbcTemplate.execute(
                "TRUNCATE visit_facts, patient_dim, disease_dim, region_dim, time_dim RESTART IDENTITY CASCADE"
        );
    }
}
