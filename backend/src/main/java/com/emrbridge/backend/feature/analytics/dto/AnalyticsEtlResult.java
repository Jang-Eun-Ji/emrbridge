package com.emrbridge.backend.feature.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyticsEtlResult {

    private final int patientsFromFhir;
    private final int encountersFromFhir;
    private final int conditionsFromFhir;
    private final int regionDimRows;
    private final int patientDimRows;
    private final int diseaseDimRows;
    private final int timeDimRows;
    private final int visitFactRows;
    private final int skippedPatients;
    private final int skippedEncounters;
}
