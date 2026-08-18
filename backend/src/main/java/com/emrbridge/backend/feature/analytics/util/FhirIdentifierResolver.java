package com.emrbridge.backend.feature.analytics.util;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FHIR 리소스 간 Reference를 Synthea ID로 해석합니다.
 * HAPI 서버는 업로드 시 {@code Patient?identifier=<syntheaId>} 형태였던 Reference를
 * 저장 후 {@code Patient/<fhir-internal-id>} 형태로 반환하므로, 내부 ID → Synthea ID 맵이 필요합니다.
 */
public final class FhirIdentifierResolver {

    public static final String PATIENT_ID_SYSTEM = "http://emrbridge.com/patient-id";
    public static final String ENCOUNTER_ID_SYSTEM = "http://emrbridge.com/encounter-id";

    private final Map<String, String> syntheaIdByFhirPatientId;
    private final Map<String, String> syntheaIdByFhirEncounterId;

    public FhirIdentifierResolver(List<Patient> patients, List<Encounter> encounters) {
        this.syntheaIdByFhirPatientId = buildPatientMap(patients);
        this.syntheaIdByFhirEncounterId = buildEncounterMap(encounters);
    }

    public String resolvePatientSyntheaId(Reference reference) {
        return resolveSyntheaId(reference, PATIENT_ID_SYSTEM, syntheaIdByFhirPatientId, "Patient");
    }

    public String resolveEncounterSyntheaId(Reference reference) {
        return resolveSyntheaId(reference, ENCOUNTER_ID_SYSTEM, syntheaIdByFhirEncounterId, "Encounter");
    }

    public String extractPatientSyntheaId(Patient patient) {
        return extractIdentifier(patient.getIdentifier(), PATIENT_ID_SYSTEM);
    }

    public String extractEncounterSyntheaId(Encounter encounter) {
        return extractIdentifier(encounter.getIdentifier(), ENCOUNTER_ID_SYSTEM);
    }

    public Map<String, List<Condition>> groupConditionsByEncounterSyntheaId(List<Condition> conditions) {
        Map<String, List<Condition>> grouped = new HashMap<>();
        for (Condition condition : conditions) {
            String encounterSyntheaId = resolveEncounterSyntheaId(condition.getEncounter());
            if (encounterSyntheaId != null) {
                grouped.computeIfAbsent(encounterSyntheaId, key -> new java.util.ArrayList<>()).add(condition);
            }
        }
        return grouped;
    }

    private static Map<String, String> buildPatientMap(List<Patient> patients) {
        Map<String, String> map = new HashMap<>();
        for (Patient patient : patients) {
            String syntheaId = extractIdentifier(patient.getIdentifier(), PATIENT_ID_SYSTEM);
            if (syntheaId != null && patient.getIdElement() != null && patient.getIdElement().hasIdPart()) {
                map.put(patient.getIdElement().getIdPart(), syntheaId);
            }
        }
        return map;
    }

    private static Map<String, String> buildEncounterMap(List<Encounter> encounters) {
        Map<String, String> map = new HashMap<>();
        for (Encounter encounter : encounters) {
            String syntheaId = extractIdentifier(encounter.getIdentifier(), ENCOUNTER_ID_SYSTEM);
            if (syntheaId != null && encounter.getIdElement() != null && encounter.getIdElement().hasIdPart()) {
                map.put(encounter.getIdElement().getIdPart(), syntheaId);
            }
        }
        return map;
    }

    private static String resolveSyntheaId(
            Reference reference,
            String system,
            Map<String, String> syntheaIdByFhirId,
            String resourceType
    ) {
        if (reference == null) {
            return null;
        }

        if (reference.hasIdentifier()) {
            Identifier identifier = reference.getIdentifier();
            if (system.equals(identifier.getSystem()) && identifier.hasValue()) {
                return identifier.getValue();
            }
            if (!identifier.hasSystem() && identifier.hasValue()) {
                return identifier.getValue();
            }
        }

        String referenceString = reference.getReference();
        if (referenceString == null) {
            return null;
        }

        if (referenceString.startsWith(resourceType + "?identifier=")) {
            String identifierPart = referenceString.substring((resourceType + "?identifier=").length());
            return parseIdentifierValue(identifierPart, system);
        }

        if (referenceString.startsWith(resourceType + "/")) {
            String fhirId = referenceString.substring((resourceType + "/").length());
            if (fhirId.contains("?")) {
                fhirId = fhirId.substring(0, fhirId.indexOf('?'));
            }
            return syntheaIdByFhirId.get(fhirId);
        }

        return null;
    }

    private static String parseIdentifierValue(String identifierPart, String system) {
        if (identifierPart.contains("|")) {
            String[] parts = identifierPart.split("\\|", 2);
            if (parts.length == 2 && (system.equals(parts[0]) || parts[0].endsWith("patient-id") || parts[0].endsWith("encounter-id"))) {
                return parts[1];
            }
            return null;
        }
        return identifierPart;
    }

    static String extractIdentifier(List<Identifier> identifiers, String system) {
        if (identifiers == null) {
            return null;
        }
        for (Identifier identifier : identifiers) {
            if (system.equals(identifier.getSystem()) && identifier.hasValue()) {
                return identifier.getValue();
            }
        }
        return null;
    }
}
