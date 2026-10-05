package com.emrbridge.backend.feature.analytics.dto;

import java.time.LocalDate;
import java.util.List;

public record PatientDetailDto(
        Integer patientId,
        Long originalPatientId,
        String firstName,
        String lastName,
        String gender,
        LocalDate birthDate,
        Integer age,
        String ageGroup,
        String regionName,
        String state,
        String city,
        LocalDate registrationDate,
        List<PatientVisitDto> visits
) {
}
