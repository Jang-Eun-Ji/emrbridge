package com.emrbridge.backend.feature.analytics.dto;

import java.util.List;

public record PipelineStatusDto(
        List<PipelineStageStatusDto> stages
) {
}
