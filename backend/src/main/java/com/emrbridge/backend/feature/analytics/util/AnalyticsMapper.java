package com.emrbridge.backend.feature.analytics.util;

import org.hl7.fhir.r4.model.Enumerations;

import java.time.LocalDate;
import java.time.Period;

public final class AnalyticsMapper {

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

    /**
     * Resolves {@code disease_category}: SNOMED code seed map first, then display-name keywords.
     */
    public static String resolveDiseaseCategory(String diseaseCode, String diseaseName) {
        return SnomedDiseaseCategoryMapping.categoryForCode(diseaseCode)
                .orElseGet(() -> resolveDiseaseCategoryByName(diseaseName));
    }

    private static String resolveDiseaseCategoryByName(String diseaseName) {
        if (diseaseName == null || diseaseName.isBlank()) {
            return SnomedDiseaseCategoryMapping.OTHER;
        }
        String lower = diseaseName.toLowerCase();
        if (containsAny(lower, "heart", "cardiac", "hypertension", "coronary", "atrial",
                "myocardial", "infarction")) {
            return SnomedDiseaseCategoryMapping.CARDIOVASCULAR;
        }
        if (containsAny(lower, "diabetes", "obesity", "obese", "metabolic", "hyperlipid",
                "hyperglycemia")) {
            return SnomedDiseaseCategoryMapping.METABOLIC;
        }
        if (containsAny(lower, "asthma", "copd", "pulmonary", "respiratory", "bronch",
                "pneumonia", "covid", "dyspnea", "wheezing")) {
            return SnomedDiseaseCategoryMapping.RESPIRATORY;
        }
        if (containsAny(lower, "depression", "anxiety", "mental", "psych", "stress",
                "alcohol", "drug abuse")) {
            return SnomedDiseaseCategoryMapping.MENTAL_HEALTH;
        }
        if (containsAny(lower, "cancer", "neoplasm", "tumor", "carcinoma", "malignant",
                "leukemia")) {
            return SnomedDiseaseCategoryMapping.ONCOLOGY;
        }
        if (containsAny(lower, "fracture", "osteo", "arthritis", "muscle", "joint",
                "sprain", "gout", "pain")) {
            return SnomedDiseaseCategoryMapping.MUSCULOSKELETAL;
        }
        if (containsAny(lower, "infection", "viral", "bacterial", "sepsis", "sinusitis",
                "cystitis", "pharyngitis")) {
            return SnomedDiseaseCategoryMapping.INFECTIOUS_DISEASE;
        }
        if (containsAny(lower, "kidney", "renal", "ckd")) {
            return SnomedDiseaseCategoryMapping.RENAL;
        }
        if (containsAny(lower, "alzheimer", "seizure", "epilepsy", "migraine", "concussion",
                "headache")) {
            return SnomedDiseaseCategoryMapping.NEUROLOGICAL;
        }
        if (containsAny(lower, "pregnan", "miscarriage", "eclampsia", "sterilization")) {
            return SnomedDiseaseCategoryMapping.PREGNANCY;
        }
        if (containsAny(lower, "laceration", "whiplash", "burn", "injury")) {
            return SnomedDiseaseCategoryMapping.INJURY;
        }
        return SnomedDiseaseCategoryMapping.OTHER;
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
