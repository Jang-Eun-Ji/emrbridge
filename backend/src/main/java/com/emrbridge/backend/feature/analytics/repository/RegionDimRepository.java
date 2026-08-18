package com.emrbridge.backend.feature.analytics.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RegionDimRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegionDimRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int upsert(String state, String county, String city, String regionName) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO region_dim (state, county, city, region_name)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (region_name) DO UPDATE SET state = EXCLUDED.state
                RETURNING region_id
                """,
                Integer.class,
                state,
                county,
                city,
                regionName
        );
    }
}
