package com.emrbridge.backend.feature.analytics.dto;

public record CategorySummaryDto(
        String diseaseCategory,
        long patientCount,
        long visitCount
) {
}
