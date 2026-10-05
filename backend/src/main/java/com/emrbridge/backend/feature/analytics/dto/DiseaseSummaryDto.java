package com.emrbridge.backend.feature.analytics.dto;

public record DiseaseSummaryDto(
        String diseaseCode,
        String diseaseName,
        String diseaseCategory,
        long patientCount,
        long visitCount
) {
}
