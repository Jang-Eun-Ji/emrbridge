package com.emrbridge.backend.feature.analytics.util;

import org.hl7.fhir.r4.model.Enumerations;

import java.time.LocalDate;
import java.time.Period;

public final class AnalyticsMapper {

    private static final String UNKNOWN_DISEASE_CODE = "UNKNOWN";
    private static final String UNKNOWN_DISEASE_NAME = "Unknown condition";

    private AnalyticsMapper() {
    }

    public static String toAnalyticsGender(Enumerations.AdministrativeGender gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender) {
            case MALE -> "M";
            case FEMALE -> "F";
            default -> null;
        };
    }

    public static int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        return Period.between(birthDate, referenceDate).getYears();
    }

    public static String toAgeGroup(int age) {
        if (age <= 10) {
            return "0-10";
        }
        if (age <= 20) {
            return "11-20";
        }
        if (age <= 30) {
            return "21-30";
        }
        if (age <= 40) {
            return "31-40";
        }
        if (age <= 50) {
            return "41-50";
        }
        if (age <= 60) {
            return "51-60";
        }
        if (age <= 70) {
            return "61-70";
        }
        return "70+";
    }

    public static String buildRegionName(String state, String city) {
        return state + ", " + city;
    }

    public static String resolveDiseaseCategory(String diseaseName) {
        if (diseaseName == null || diseaseName.isBlank()) {
            return "Other";
        }
        String lower = diseaseName.toLowerCase();
        if (containsAny(lower, "heart", "cardiac", "hypertension", "coronary", "atrial")) {
            return "Cardiovascular";
        }
        if (containsAny(lower, "diabetes", "obesity", "metabolic")) {
            return "Metabolic";
        }
        if (containsAny(lower, "asthma", "copd", "pulmonary", "respiratory", "bronch")) {
            return "Respiratory";
        }
        if (containsAny(lower, "depression", "anxiety", "mental", "psych")) {
            return "Mental Health";
        }
        if (containsAny(lower, "cancer", "neoplasm", "tumor", "carcinoma", "malignant")) {
            return "Oncology";
        }
        if (containsAny(lower, "fracture", "osteo", "arthritis", "muscle", "joint")) {
            return "Musculoskeletal";
        }
        if (containsAny(lower, "infection", "viral", "bacterial", "sepsis", "pneumonia")) {
            return "Infectious Disease";
        }
        return "Other";
    }

    public static String unknownDiseaseCode() {
        return UNKNOWN_DISEASE_CODE;
    }

    public static String unknownDiseaseName() {
        return UNKNOWN_DISEASE_NAME;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
