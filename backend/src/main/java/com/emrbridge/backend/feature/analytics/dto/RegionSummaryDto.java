package com.emrbridge.backend.feature.analytics.dto;

public record RegionSummaryDto(
        String state,
        String city,
        String regionName,
        long patientCount,
        long visitCount
) {
}
