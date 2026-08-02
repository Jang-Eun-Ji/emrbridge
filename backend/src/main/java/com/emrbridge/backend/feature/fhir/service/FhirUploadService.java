package com.emrbridge.backend.feature.fhir.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.emrbridge.backend.feature.emr.domain.*;
import com.emrbridge.backend.feature.emr.repository.*;
import com.emrbridge.backend.feature.fhir.converter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@Service
@RequiredArgsConstructor
public class FhirUploadService {

    private final IGenericClient fhirClient;
    private final EmrPatientRepository patientRepo;
    private final EmrOrganizationRepository organizationRepo;
    private final EmrProviderRepository providerRepo;
    private final PatientConverter patientConverter;
    private final EmrEncounterRepository encounterRepo;
    private final EmrConditionRepository conditionRepo;
    private final EmrObservationRepository observationRepo;
    private final EmrMedicationRepository medicationRepo;
    private final EmrProcedureRepository procedureRepo;
    private final EmrAllergyRepository allergyRepo;
    private final EmrImmunizationRepository immunizationRepo;
    private final EmrCareplanRepository careplanRepo;

    private final EncounterConverter encounterConverter;
    private final OrganizationConverter organizationConverter;
    private final ProviderConverter providerConverter;
    private final ConditionConverter conditionConverter;
    private final ObservationConverter observationConverter;
    private final MedicationConverter medicationConverter;
    private final ProcedureConverter procedureConverter;
    private final AllergyConverter allergyConverter;
    private final ImmunizationConverter immunizationConverter;
    private final CareplanConverter careplanConverter;

    public Map<String, Integer> uploadAll() {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("organizations",  uploadAllOrganizations());
        result.put("providers",      uploadAllProviders());
        result.put("patients",       uploadAllPatients());
        result.put("encounters",     uploadAllEncounters());
        result.put("conditions",     uploadAllConditions());
        result.put("observations",   uploadAllObservations());
        result.put("medications",    uploadAllMedications());
        result.put("procedures",     uploadAllProcedures());
        result.put("allergies",      uploadAllAllergies());
        result.put("immunizations",  uploadAllImmunizations());
        result.put("careplans",      uploadAllCareplans());
        return result;
    }

    /**
     * Uploads all EmrPatient records to the FHIR server using conditional
     * create, matched on SSN (http://hl7.org/fhir/sid/us-ssn) rather than the
     * source-specific syntheaPatientId.
     * <p>
     * Matching on SSN means that if the same real-world person appears in
     * multiple independent EMR sources (each assigning their own
     * syntheaPatientId), the FHIR server recognizes them as a single existing
     * Patient resource instead of creating a duplicate. Patients with no SSN
     * on file are skipped, since conditional create requires a non-empty
     * match value to avoid an unconstrained "create or match everything" query.
     *
     * @return number of patients successfully uploaded (created or matched)
     */
    @Transactional(readOnly = true)
    public int uploadAllPatients() {
        int count = 0;
        for (EmrPatient emr : patientRepo.findAll()) {
            // SSN is now the cross-EMR identity key; without one, conditional
            // create has no reliable way to detect the same person arriving
            // from a different source, so these are skipped rather than risking
            // duplicate or unmatched Patient resources.
            if (emr.getSsn() == null || emr.getSsn().isBlank()) {
                log.warn("Patient skipped - no SSN on file, syntheaId: {}", emr.getSyntheaPatientId());
                continue;
            }

            try {
                fhirClient.create()
                        .resource(patientConverter.toFhir(emr))
                        .conditional()
                        .where(Patient.IDENTIFIER.exactly()
                                .systemAndIdentifier(
                                        "http://hl7.org/fhir/sid/us-ssn",
                                        emr.getSsn()))
                        .execute();
                count++;
            } catch (Exception e) {
                log.error("Patient saving failed - syntheaId: {}, ssn: {}, error: {}",
                        emr.getSyntheaPatientId(), emr.getSsn(), e.getMessage());
            }
        }
        log.info("Patient FHIR 업로드: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllEncounters() {
        int count = 0;
        for (EmrEncounter emr : encounterRepo.findAll()) {
            try {
                MethodOutcome outcome = fhirClient.create()
                        .resource(encounterConverter.toFhir(emr))
                        .conditional()
                        .where(Encounter.IDENTIFIER.exactly()
                                .systemAndIdentifier(
                                        "http://emrbridge.com/encounter-id",
                                        emr.getSyntheaId()))
                        .execute();

                count++;
            } catch (Exception e) {
                // Log and skip this encounter; do not interrupt the batch.
                log.error("Encounter 전송 실패 - syntheaId: {}, error: {}",
                        emr.getSyntheaId(), e.getMessage());
            }
        }

        log.info("Encounter FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllOrganizations() {
        int count = 0;
        for (EmrOrganization emr : organizationRepo.findAll()) {
            try {
                fhirClient.create()
                        .resource(organizationConverter.toFhir(emr))
                        .conditional()
                        .where(Organization.IDENTIFIER.exactly()
                                .systemAndIdentifier(
                                        "http://emrbridge.com/organization-id",
                                        emr.getSyntheaId()))
                        .execute();
                count++;
            } catch (Exception e) {
                log.error("Organization 전송 실패 - syntheaId: {}, error: {}", emr.getSyntheaId(), e.getMessage());
            }
        }
        log.info("Organization FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllProviders() {
        int count = 0;
        for (EmrProvider emr : providerRepo.findAll()) {
            try {
                fhirClient.create()
                        .resource(providerConverter.toFhir(emr))
                        .conditional()
                        .where(Practitioner.IDENTIFIER.exactly()
                                .systemAndIdentifier(
                                        "http://emrbridge.com/provider-id",
                                        emr.getSyntheaId()))
                        .execute();
                count++;
            } catch (Exception e) {
                log.error("Provider 전송 실패 - syntheaId: {}, error: {}", emr.getSyntheaId(), e.getMessage());
            }
        }
        log.info("Provider FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllConditions() {
        int count = 0;
        for (EmrCondition emr : conditionRepo.findAll()) {
            try {
                fhirClient.create().resource(conditionConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Condition 전송 실패 - id: {}, error: {}", emr.getId(), e.getMessage());
            }
        }
        log.info("Condition FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllObservations() {
        int count = 0;
        for (EmrObservation emr : observationRepo.findAll()) {
            try {
                fhirClient.create().resource(observationConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Observation 전송 실패 - id: {}, error: {}", emr.getId(), e.getMessage());
            }
        }
        log.info("Observation FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllMedications() {
        int count = 0;
        for (EmrMedication emr : medicationRepo.findAll()) {
            try {
                fhirClient.create().resource(medicationConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Medication 전송 실패 - id: {}, error: {}", emr.getId(), e.getMessage());
            }
        }
        log.info("Medication FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllProcedures() {
        int count = 0;
        for (EmrProcedure emr : procedureRepo.findAll()) {
            try {
                fhirClient.create().resource(procedureConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Procedure 전송 실패 - id: {}, error: {}", emr.getId(), e.getMessage());
            }
        }
        log.info("Procedure FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllAllergies() {
        int count = 0;
        for (EmrAllergy emr : allergyRepo.findAll()) {
            try {
                fhirClient.create().resource(allergyConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Allergy 전송 실패 - id: {}, error: {}", emr.getId(), e.getMessage());
            }
        }
        log.info("Allergy FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllImmunizations() {
        int count = 0;
        for (EmrImmunization emr : immunizationRepo.findAll()) {
            try {
                fhirClient.create().resource(immunizationConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Immunization 전송 실패 - id: {}, error: {}", emr.getId(), e.getMessage());
            }
        }
        log.info("Immunization FHIR 업로드 완료: {}건", count);
        return count;
    }

    @Transactional(readOnly = true)
    public int uploadAllCareplans() {
        int count = 0;
        for (EmrCareplan emr : careplanRepo.findAll()) {
            try {
                fhirClient.create().resource(careplanConverter.toFhir(emr)).execute();
                count++;
            } catch (Exception e) {
                log.error("Careplan 전송 실패 - syntheaId: {}, error: {}", emr.getSyntheaId(), e.getMessage());
            }
        }
        log.info("Careplan FHIR 업로드 완료: {}건", count);
        return count;
    }
}