package com.emrbridge.backend.feature.analytics.dto;

import java.time.LocalDateTime;

public record PipelineStageStatusDto(
        String key,
        String status,
        Long recordCount,
        LocalDateTime lastRunAt,
        String message
) {
}
