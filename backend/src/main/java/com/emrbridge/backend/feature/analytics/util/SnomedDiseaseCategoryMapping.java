package com.emrbridge.backend.feature.analytics.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hardcoded SNOMED CT code → Analytics {@code disease_category} for Synthea conditions.csv.
 */
public final class SnomedDiseaseCategoryMapping {

    public static final String CARDIOVASCULAR = "Cardiovascular";
    public static final String METABOLIC = "Metabolic";
    public static final String RESPIRATORY = "Respiratory";
    public static final String MENTAL_HEALTH = "Mental Health";
    public static final String ONCOLOGY = "Oncology";
    public static final String MUSCULOSKELETAL = "Musculoskeletal";
    public static final String INFECTIOUS_DISEASE = "Infectious Disease";
    public static final String RENAL = "Renal";
    public static final String NEUROLOGICAL = "Neurological";
    public static final String GASTROINTESTINAL = "Gastrointestinal";
    public static final String DERMATOLOGIC = "Dermatologic";
    public static final String DENTAL = "Dental";
    public static final String PREGNANCY = "Pregnancy";
    public static final String INJURY = "Injury";
    public static final String HEMATOLOGIC = "Hematologic";
    public static final String SOCIAL_DETERMINANTS = "Social Determinants";
    public static final String OTHER = "Other";

    private static final Map<String, String> BY_CODE;

    static {
        Map<String, String> m = new HashMap<>();

        // Cardiovascular
        m.put("274531002", CARDIOVASCULAR);
        m.put("401314000", CARDIOVASCULAR);
        m.put("401303003", CARDIOVASCULAR);
        m.put("60234000", CARDIOVASCULAR);
        m.put("49436004", CARDIOVASCULAR);
        m.put("88805009", CARDIOVASCULAR);
        m.put("706870000", CARDIOVASCULAR);
        m.put("59621000", CARDIOVASCULAR);
        m.put("399261000", CARDIOVASCULAR);
        m.put("399211009", CARDIOVASCULAR);
        m.put("414545008", CARDIOVASCULAR);
        m.put("22298006", CARDIOVASCULAR);

        // Metabolic
        m.put("162864005", METABOLIC);
        m.put("408512008", METABOLIC);
        m.put("44054006", METABOLIC);
        m.put("127013003", METABOLIC);
        m.put("80394007", METABOLIC);
        m.put("55822004", METABOLIC);
        m.put("302870006", METABOLIC);
        m.put("237602007", METABOLIC);
        m.put("90781000119102", METABOLIC);
        m.put("368581000119106", METABOLIC);
        m.put("1551000119108", METABOLIC);
        m.put("714628002", METABOLIC);
        m.put("157141000119108", METABOLIC);

        // Respiratory
        m.put("10509002", RESPIRATORY);
        m.put("65710008", RESPIRATORY);
        m.put("195967001", RESPIRATORY);
        m.put("233678006", RESPIRATORY);
        m.put("185086009", RESPIRATORY);
        m.put("49727002", RESPIRATORY);
        m.put("840539006", RESPIRATORY);
        m.put("267036007", RESPIRATORY);
        m.put("389087006", RESPIRATORY);
        m.put("68235000", RESPIRATORY);
        m.put("78275009", RESPIRATORY);
        m.put("233604007", RESPIRATORY);
        m.put("87433001", RESPIRATORY);
        m.put("271825005", RESPIRATORY);
        m.put("73430006", RESPIRATORY);
        m.put("39898005", RESPIRATORY);
        m.put("248595008", RESPIRATORY);
        m.put("840544004", RESPIRATORY);
        m.put("56018004", RESPIRATORY);

        // Mental Health
        m.put("7200002", MENTAL_HEALTH);
        m.put("6525002", MENTAL_HEALTH);
        m.put("361055000", MENTAL_HEALTH);
        m.put("1149222004", MENTAL_HEALTH);
        m.put("80583007", MENTAL_HEALTH);
        m.put("73595000", MENTAL_HEALTH);
        m.put("10939881000119105", MENTAL_HEALTH);

        // Oncology
        m.put("93143009", ONCOLOGY);
        m.put("254837009", ONCOLOGY);
        m.put("109838007", ONCOLOGY);
        m.put("68496003", ONCOLOGY);
        m.put("713197008", ONCOLOGY);
        m.put("315268008", ONCOLOGY);

        // Musculoskeletal
        m.put("203646004", MUSCULOSKELETAL);
        m.put("278860009", MUSCULOSKELETAL);
        m.put("1121000119107", MUSCULOSKELETAL);
        m.put("82423001", MUSCULOSKELETAL);
        m.put("359817006", MUSCULOSKELETAL);
        m.put("203082005", MUSCULOSKELETAL);
        m.put("16114001", MUSCULOSKELETAL);
        m.put("125605004", MUSCULOSKELETAL);
        m.put("58150001", MUSCULOSKELETAL);
        m.put("65966004", MUSCULOSKELETAL);
        m.put("263172003", MUSCULOSKELETAL);
        m.put("263102004", MUSCULOSKELETAL);
        m.put("90560007", MUSCULOSKELETAL);
        m.put("444470001", MUSCULOSKELETAL);
        m.put("125601008", MUSCULOSKELETAL);
        m.put("444448004", MUSCULOSKELETAL);
        m.put("57676002", MUSCULOSKELETAL);
        m.put("201834006", MUSCULOSKELETAL);
        m.put("68962001", MUSCULOSKELETAL);
        m.put("239872002", MUSCULOSKELETAL);
        m.put("239873007", MUSCULOSKELETAL);
        m.put("64859006", MUSCULOSKELETAL);
        m.put("30832001", MUSCULOSKELETAL);
        m.put("384709000", MUSCULOSKELETAL);
        m.put("44465007", MUSCULOSKELETAL);
        m.put("70704007", MUSCULOSKELETAL);

        // Infectious Disease
        m.put("75498004", INFECTIOUS_DISEASE);
        m.put("307426000", INFECTIOUS_DISEASE);
        m.put("195662009", INFECTIOUS_DISEASE);
        m.put("40055000", INFECTIOUS_DISEASE);
        m.put("427898007", INFECTIOUS_DISEASE);
        m.put("65363002", INFECTIOUS_DISEASE);
        m.put("197927001", INFECTIOUS_DISEASE);
        m.put("91302008", INFECTIOUS_DISEASE);
        m.put("770349000", INFECTIOUS_DISEASE);
        m.put("36971009", INFECTIOUS_DISEASE);
        m.put("267102003", INFECTIOUS_DISEASE);
        m.put("43878008", INFECTIOUS_DISEASE);
        m.put("444814009", INFECTIOUS_DISEASE);

        // Renal
        m.put("431855005", RENAL);
        m.put("431856006", RENAL);
        m.put("433144002", RENAL);
        m.put("431857002", RENAL);
        m.put("46177005", RENAL);
        m.put("161665007", RENAL);
        m.put("698306007", RENAL);

        // Neurological
        m.put("26929004", NEUROLOGICAL);
        m.put("124171000119105", NEUROLOGICAL);
        m.put("110030002", NEUROLOGICAL);
        m.put("62106007", NEUROLOGICAL);
        m.put("84757009", NEUROLOGICAL);
        m.put("230265002", NEUROLOGICAL);
        m.put("25064002", NEUROLOGICAL);
        m.put("1290882004", NEUROLOGICAL);
        m.put("36955009", NEUROLOGICAL);
        m.put("128613002", NEUROLOGICAL);
        m.put("127294003", NEUROLOGICAL);

        // Gastrointestinal
        m.put("65275009", GASTROINTESTINAL);
        m.put("235919008", GASTROINTESTINAL);
        m.put("428251008", GASTROINTESTINAL);

        // Dermatologic
        m.put("241929008", DERMATOLOGIC);
        m.put("24079001", DERMATOLOGIC);
        m.put("48333001", DERMATOLOGIC);
        m.put("40275004", DERMATOLOGIC);
        m.put("403190006", DERMATOLOGIC);
        m.put("403191005", DERMATOLOGIC);
        m.put("446096008", DERMATOLOGIC);
        m.put("232353008", DERMATOLOGIC);
        m.put("367498001", DERMATOLOGIC);

        // Dental
        m.put("61804006", DENTAL);
        m.put("278558000", DENTAL);
        m.put("278588009", DENTAL);
        m.put("18718003", DENTAL);
        m.put("66383009", DENTAL);
        m.put("196416002", DENTAL);
        m.put("278598003", DENTAL);
        m.put("278602001", DENTAL);
        m.put("37320007", DENTAL);
        m.put("109570002", DENTAL);
        m.put("1255252008", DENTAL);
        m.put("11625007", DENTAL);
        m.put("46752004", DENTAL);

        // Pregnancy
        m.put("35999006", PREGNANCY);
        m.put("156073000", PREGNANCY);
        m.put("198992004", PREGNANCY);
        m.put("267020005", PREGNANCY);
        m.put("19169002", PREGNANCY);
        m.put("72892002", PREGNANCY);
        m.put("161744009", PREGNANCY);
        m.put("398254007", PREGNANCY);
        m.put("183996000", PREGNANCY);

        // Injury
        m.put("370247008", INJURY);
        m.put("90460009", INJURY);
        m.put("312608009", INJURY);
        m.put("284551006", INJURY);
        m.put("283371005", INJURY);
        m.put("284549007", INJURY);
        m.put("283385000", INJURY);
        m.put("39848009", INJURY);

        // Hematologic
        m.put("271737000", HEMATOLOGIC);
        m.put("698303004", HEMATOLOGIC);
        m.put("108631000119101", HEMATOLOGIC);

        // Social Determinants
        m.put("473461003", SOCIAL_DETERMINANTS);
        m.put("160903007", SOCIAL_DETERMINANTS);
        m.put("266948004", SOCIAL_DETERMINANTS);
        m.put("105531004", SOCIAL_DETERMINANTS);
        m.put("713458007", SOCIAL_DETERMINANTS);
        m.put("423315002", SOCIAL_DETERMINANTS);
        m.put("314529007", SOCIAL_DETERMINANTS);
        m.put("741062008", SOCIAL_DETERMINANTS);
        m.put("224295006", SOCIAL_DETERMINANTS);
        m.put("160904001", SOCIAL_DETERMINANTS);
        m.put("224299000", SOCIAL_DETERMINANTS);
        m.put("446654005", SOCIAL_DETERMINANTS);
        m.put("424393004", SOCIAL_DETERMINANTS);
        m.put("160968000", SOCIAL_DETERMINANTS);
        m.put("1187604002", SOCIAL_DETERMINANTS);
        m.put("422650009", SOCIAL_DETERMINANTS);
        m.put("266934004", SOCIAL_DETERMINANTS);
        m.put("73438004", SOCIAL_DETERMINANTS);
        m.put("706893006", SOCIAL_DETERMINANTS);

        // Other (nonspecific symptoms)
        m.put("84229001", OTHER);
        m.put("386661006", OTHER);

        BY_CODE = Collections.unmodifiableMap(m);
    }

    private SnomedDiseaseCategoryMapping() {
    }

    public static Optional<String> categoryForCode(String snomedCode) {
        if (snomedCode == null || snomedCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(snomedCode.trim()));
    }
}
