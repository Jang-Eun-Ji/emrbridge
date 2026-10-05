package com.emrbridge.backend.feature.analytics.dto;

import java.time.LocalDate;

public record PatientVisitDto(
        Integer visitId,
        LocalDate visitDate,
        String visitType,
        String diseaseCode,
        String diseaseName,
        String diseaseCategory
) {
}
