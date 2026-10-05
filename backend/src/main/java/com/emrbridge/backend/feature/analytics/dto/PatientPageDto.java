package com.emrbridge.backend.feature.analytics.dto;

import java.util.List;

public record PatientPageDto(
        List<PatientSummaryDto> items,
        long total
) {
}
