package com.emrbridge.backend.feature.analytics.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TimeDimRepository {

    private final JdbcTemplate jdbcTemplate;

    public TimeDimRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int upsert(int year, int month, int quarter, String yearMonth) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO time_dim (year, month, quarter, year_month)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (year_month) DO UPDATE SET year = EXCLUDED.year
                RETURNING time_id
                """,
                Integer.class,
                year,
                month,
                quarter,
                yearMonth
        );
    }
}
