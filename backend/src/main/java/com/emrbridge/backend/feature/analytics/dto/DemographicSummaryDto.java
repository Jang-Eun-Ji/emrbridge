package com.emrbridge.backend.feature.analytics.dto;

public record DemographicSummaryDto(
        String ageGroup,
        String gender,
        long patientCount,
        long visitCount
) {
}
