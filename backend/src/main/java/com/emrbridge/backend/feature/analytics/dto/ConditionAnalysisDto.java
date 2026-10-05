package com.emrbridge.backend.feature.analytics.dto;

import java.util.List;

public record ConditionAnalysisDto(
        List<CategorySummaryDto> categories,
        List<DiseaseSummaryDto> diseases
) {
}
