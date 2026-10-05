package com.emrbridge.backend.feature.analytics.dto;

import java.time.LocalDate;

public record PatientSummaryDto(
        Integer patientId,
        Long originalPatientId,
        String firstName,
        String lastName,
        String gender,
        LocalDate birthDate,
        Integer age,
        String ageGroup,
        String regionName
) {
}
