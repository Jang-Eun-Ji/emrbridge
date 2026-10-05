package com.emrbridge.backend.feature.analytics.dto;

public record MonthlyVisitSummaryDto(
        String yearMonth,
        int year,
        int month,
        long totalVisits,
        long uniquePatients
) {
}
