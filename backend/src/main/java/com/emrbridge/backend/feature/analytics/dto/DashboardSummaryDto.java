package com.emrbridge.backend.feature.analytics.dto;

import java.util.List;

public record DashboardSummaryDto(
        long totalPatients,
        long totalVisits,
        long totalDiseases,
        long totalRegions,
        List<MonthlyVisitSummaryDto> monthlyVisits,
        List<DemographicSummaryDto> demographics,
        List<DiseaseSummaryDto> topDiseases,
        List<RegionSummaryDto> regions
) {
}
