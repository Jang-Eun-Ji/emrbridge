package com.emrbridge.backend.feature.emr.service;

import com.emrbridge.backend.feature.emr.domain.*;
import com.emrbridge.backend.feature.emr.dto.*;
import com.emrbridge.backend.feature.emr.repository.*;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;


/**
 * Imports Synthea-generated CSV files into the EMR database.
 * <p>
 * All entity types are imported using an "upsert" strategy: if a record with
 * the same natural key (Synthea id, or a composite key for line-item entities
 * that have no id column in the source CSV) already exists in the database,
 * its fields are updated in place; otherwise a new row is inserted.
 * <p>
 * This makes {@link #importAll()} safe to re-run multiple times against the
 * same CSV files (idempotent import) without violating unique constraints.
 */
@Service
@RequiredArgsConstructor
public class SyntheaImportService {

    private final EmrOrganizationRepository orgRepo;
    private final EmrProviderRepository providerRepo;
    private final EmrPatientRepository patientRepo;
    private final EmrEncounterRepository encounterRepo;
    private final EmrConditionRepository conditionRepo;
    private final EmrObservationRepository observationRepo;
    private final EmrMedicationRepository medicationRepo;
    private final EmrProcedureRepository procedureRepo;
    private final EmrAllergyRepository allergyRepo;
    private final EmrImmunizationRepository immunizationRepo;
    private final EmrCareplanRepository careplanRepo;
    private final EntityManager entityManager;

    @Value("${csv.import.path}")
    private String csvImportPath;

    // In-memory caches keyed by Synthea id, used to resolve FK relationships
    // (e.g. an encounter's patient) without re-querying the DB for every row.
    // These are rebuilt from the DB at the start of every importAll() call,
    // so re-running the import always sees previously-imported records.
    private Map<String, EmrPatient>      patientCache      = new HashMap<>();
    private Map<String, EmrEncounter>    encounterCache    = new HashMap<>();
    private Map<String, EmrOrganization> organizationCache = new HashMap<>();
    private Map<String, EmrProvider>     providerCache     = new HashMap<>();

    /**
     * Runs the full import pipeline in dependency order (organizations and
     * providers must exist before patients/encounters can reference them, and
     * so on down the chain).
     *
     * @return a map of entity type name -> number of rows upserted (inserted or updated)
     * @throws RuntimeException if any CSV file cannot be read or parsed
     */
    @Transactional
    public Map<String, Integer> importAll() {
        // Rebuild all FK-resolution caches from the DB so that re-running the
        // import is idempotent: previously-imported rows are recognized and
        // updated instead of re-inserted.
        patientCache = patientRepo.findAll().stream()
                .collect(Collectors.toMap(EmrPatient::getSyntheaPatientId, p -> p));
        organizationCache = orgRepo.findAll().stream()
                .collect(Collectors.toMap(EmrOrganization::getSyntheaId, o -> o));
        providerCache = providerRepo.findAll().stream()
                .collect(Collectors.toMap(EmrProvider::getSyntheaId, p -> p));
        encounterCache = encounterRepo.findAll().stream()
                .collect(Collectors.toMap(EmrEncounter::getSyntheaId, e -> e));

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("organizations",  importOrganizations());
        result.put("providers",      importProviders());
        result.put("patients",       importPatients());
        result.put("encounters",     importEncounters());
        result.put("conditions",     importConditions());
        result.put("observations",   importObservations());
        result.put("medications",    importMedications());
        result.put("procedures",     importProcedures());
        result.put("allergies",      importAllergies());
        result.put("immunizations",  importImmunizations());
        result.put("careplans",      importCareplans());
        return result;
    }

    /**
     * Upserts organizations.csv into emr_organization.
     * Natural key: Synthea organization Id.
     *
     * @return number of rows processed (inserted or updated)
     */
    public int importOrganizations() {
        List<OrganizationCsvRow> rows = parseCsv("organizations.csv", OrganizationCsvRow.class);
        int count = 0;
        for (OrganizationCsvRow r : rows) {
            // Skip malformed rows with no id - cannot upsert without a key.
            if (r.getId() == null || r.getId().isBlank()) continue;

            // Look up existing row by natural key; reuse it if found (update),
            // otherwise start a new entity (insert).
            EmrOrganization org = organizationCache.getOrDefault(
                    r.getId(), EmrOrganization.builder().syntheaId(r.getId()).build());

            org.setName(r.getName());
            org.setAddress(r.getAddress());
            org.setCity(r.getCity());
            org.setState(r.getState());
            org.setZip(r.getZip());
            org.setLat(parseDouble(r.getLat()));
            org.setLon(parseDouble(r.getLon()));
            org.setPhone(r.getPhone());

            orgRepo.save(org); // INSERT if id is null, UPDATE otherwise (JPA save semantics)
            organizationCache.put(r.getId(), org);
            count++;
        }
        return count;
    }

    /**
     * Upserts providers.csv into emr_provider.
     * Natural key: Synthea provider Id.
     *
     * @return number of rows processed (inserted or updated)
     */
    public int importProviders() {
        List<ProviderCsvRow> rows = parseCsv("providers.csv", ProviderCsvRow.class);
        int count = 0;
        for (ProviderCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;

            EmrProvider provider = providerCache.getOrDefault(
                    r.getId(), EmrProvider.builder().syntheaId(r.getId()).build());

            EmrOrganization org = organizationCache.get(r.getOrganization());

            provider.setOrganization(org);
            provider.setName(r.getName());
            provider.setGender(r.getGender());
            provider.setSpeciality(r.getSpeciality());
            provider.setAddress(r.getAddress());
            provider.setCity(r.getCity());
            provider.setState(r.getState());
            provider.setZip(r.getZip());
            provider.setLat(parseDouble(r.getLat()));
            provider.setLon(parseDouble(r.getLon()));
            provider.setUtilization(parseInt(r.getUtilization()));

            providerRepo.save(provider);
            providerCache.put(r.getId(), provider);
            count++;
        }
        return count;
    }

    /**
     * Upserts patients.csv into emr_patient.
     * Natural key: Synthea patient Id.
     *
     * @return number of rows processed (inserted or updated)
     * @throws RuntimeException if patients.csv cannot be read or parsed
     */
    public int importPatients() {
        List<PatientCsvRow> rows = parseCsv("patients.csv", PatientCsvRow.class);

        int count = 0;

            for (PatientCsvRow row : rows) {
                if (row.getId() == null || row.getId().isBlank()) {
                    continue;
                }

                EmrPatient patient = patientCache.getOrDefault(
                        row.getId(), EmrPatient.builder().syntheaPatientId(row.getId()).build());
                patient.setSsn(row.getSsn());
                patient.setFirstName(row.getFirst());
                patient.setLastName(row.getLast());
                patient.setGender(row.getGender());
                patient.setBirthDate(parseDate(row.getBirthDate()));
                patient.setDeathDate(parseDate(row.getDeathDate()));
                patient.setRace(row.getRace());
                patient.setEthnicity(row.getEthnicity());
                patient.setAddress(row.getAddress());
                patient.setCity(row.getCity());
                patient.setState(row.getState());
                patient.setCounty(row.getCounty());
                patient.setZip(row.getZip());
                patient.setLat(parseDouble(row.getLat()));
                patient.setLon(parseDouble(row.getLon()));
                patient.setHealthcareExpenses(parseBigDecimal(row.getHealthcareExpenses()));
                patient.setHealthcareCoverage(parseBigDecimal(row.getHealthcareCoverage()));

                patientRepo.save(patient);
                patientCache.put(row.getId(), patient); // keep cache in sync after save
                count++;
            }

            return count;
    }

    /**
     * Upserts encounters.csv into emr_encounter.
     * Natural key: Synthea encounter Id.
     * Rows referencing an unknown patient are skipped, since encounter
     * cannot exist without its owning patient.
     *
     * @return number of rows processed (inserted or updated)
     */
    public int importEncounters() {
        List<EncounterCsvRow> rows = parseCsv("encounters.csv", EncounterCsvRow.class);
        int count = 0;
        for (EncounterCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;

            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue; // cannot upsert an encounter with no patient

            EmrEncounter encounter = encounterCache.getOrDefault(
                    r.getId(), EmrEncounter.builder().syntheaId(r.getId()).build());

            EmrOrganization org      = organizationCache.get(r.getOrganization());
            EmrProvider     provider = providerCache.get(r.getProvider());

            encounter.setPatient(patient);
            encounter.setOrganization(org);
            encounter.setProvider(provider);
            encounter.setStartTime(parseDateTime(r.getStart()));
            encounter.setStopTime(parseDateTime(r.getStop()));
            encounter.setEncounterClass(r.getEncounterClass());
            encounter.setCode(r.getCode());
            encounter.setDescription(r.getDescription());
            encounter.setBaseEncounterCost(parseBigDecimal(r.getBaseEncounterCost()));
            encounter.setTotalClaimCost(parseBigDecimal(r.getTotalClaimCost()));
            encounter.setPayerCoverage(parseBigDecimal(r.getPayerCoverage()));
            encounter.setReasonCode(r.getReasonCode());
            encounter.setReasonDescription(r.getReasonDescription());

            encounterRepo.save(encounter);
            encounterCache.put(r.getId(), encounter);
            count++;
        }
        return count;
    }

    /**
     * Upserts conditions.csv into emr_condition.
     * Natural key: composite (patient, encounter, code, startDate).
     * See {@link #importObservations()} for the in-memory duplicate-check and
     * batched-flush performance rationale.
     *
     * @return number of rows newly inserted (exact duplicates are skipped)
     */
    public int importConditions() {
        List<ConditionCsvRow> rows = parseCsv("conditions.csv", ConditionCsvRow.class);

        Set<String> existingKeys = conditionRepo.findAll().stream()
                .map(c -> buildConditionKey(
                        c.getPatient() != null ? c.getPatient().getId() : null,
                        c.getEncounter() != null ? c.getEncounter().getId() : null,
                        c.getCode(), c.getStartDate()))
                .collect(Collectors.toCollection(HashSet::new));

        int count = 0;
        int pendingSinceFlush = 0;

        for (ConditionCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDate startDate = parseDate(r.getStart());

            String key = buildConditionKey(patient.getId(),
                    encounter != null ? encounter.getId() : null,
                    r.getCode(), startDate);

            if (existingKeys.contains(key)) continue;
            existingKeys.add(key);

            EmrCondition condition = EmrCondition.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .code(r.getCode())
                    .startDate(startDate)
                    .stopDate(parseDate(r.getStop()))
                    .description(r.getDescription())
                    .build();

            conditionRepo.save(condition);
            count++;
            pendingSinceFlush++;

            if (pendingSinceFlush >= 1000) {
                entityManager.flush();
                pendingSinceFlush = 0;
            }
        }

        if (pendingSinceFlush > 0) {
            entityManager.flush();
        }

        return count;
    }

    /**
     * Builds a composite string key for in-memory duplicate detection of
     * condition rows. Null-safe: encounter may be null.
     */
    private String buildConditionKey(Long patientId, Long encounterId, String code, LocalDate startDate) {
        return patientId + "|" + encounterId + "|" + code + "|" + startDate;
    }

    /**
     * Upserts observations.csv into emr_observation.
     * Natural key: composite (patient, encounter, code, obsDate, value).
     * <p>
     * Performance: instead of issuing one SELECT per CSV row to check for an
     * existing exact-duplicate match (previously turned a multi-million-row
     * CSV into a multi-million-round-trip operation), this loads all existing
     * (patient, encounter, code, obsDate, value) keys into an in-memory Set
     * once at the start of the method. Each CSV row is then checked against
     * that Set (O(1), no DB round trip). Rows are also batched and flushed
     * every 1000 records (paired with hibernate.jdbc.batch_size=1000 in
     * application.yml) so Hibernate can group them into batched INSERT
     * statements instead of one round trip per row. The persistence context
     * is intentionally NOT cleared between flushes, since patientCache/
     * encounterCache hold managed entity references that FK relationships
     * depend on; this trades higher memory usage for avoiding detached-entity
     * errors on very large imports.
     *
     * @return number of rows newly inserted (exact duplicates are skipped)
     */
    public int importObservations() {
        List<ObservationCsvRow> rows = parseCsv("observations.csv", ObservationCsvRow.class);

        // Load all existing observation keys into memory once, instead of
        // querying per row. Uses a composite string key for fast Set lookups.
        Set<String> existingKeys = observationRepo.findAll().stream()
                .map(o -> buildObservationKey(
                        o.getPatient() != null ? o.getPatient().getId() : null,
                        o.getEncounter() != null ? o.getEncounter().getId() : null,
                        o.getCode(), o.getObsDate(), o.getValue()))
                .collect(Collectors.toCollection(HashSet::new));

        int count = 0;
        int pendingSinceFlush = 0;

        for (ObservationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime obsDate = parseDateTime(r.getDate());

            String key = buildObservationKey(patient.getId(),
                    encounter != null ? encounter.getId() : null,
                    r.getCode(), obsDate, r.getValue());

            // In-memory duplicate check - no DB round trip.
            if (existingKeys.contains(key)) continue;
            existingKeys.add(key); // guard against duplicates within this same CSV file

            EmrObservation observation = EmrObservation.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .code(r.getCode())
                    .obsDate(obsDate)
                    .value(r.getValue())
                    .category(r.getCategory())
                    .description(r.getDescription())
                    .units(r.getUnits())
                    .obsType(r.getType())
                    .build();

            observationRepo.save(observation);
            count++;
            pendingSinceFlush++;

            // Flush periodically so Hibernate groups pending inserts into
            // batched statements (see hibernate.jdbc.batch_size). The
            // persistence context is deliberately not cleared - see javadoc.
            if (pendingSinceFlush >= 1000) {
                entityManager.flush();
                pendingSinceFlush = 0;
            }
        }

        // Flush any remaining rows that didn't reach a full batch.
        if (pendingSinceFlush > 0) {
            entityManager.flush();
        }

        return count;
    }

    /**
     * Builds a composite string key for in-memory duplicate detection of
     * observation rows. Null-safe: encounter may legitimately be null.
     */
    private String buildObservationKey(Long patientId, Long encounterId, String code,
                                       LocalDateTime obsDate, String value) {
        return patientId + "|" + encounterId + "|" + code + "|" + obsDate + "|" + value;
    }

    /**
     * Upserts medications.csv into emr_medication.
     * Natural key: composite (patient, encounter, code, startTime, dispenses).
     * See {@link #importObservations()} for the in-memory duplicate-check and
     * batched-flush performance rationale.
     *
     * @return number of rows newly inserted (exact duplicates are skipped)
     */
    public int importMedications() {
        List<MedicationCsvRow> rows = parseCsv("medications.csv", MedicationCsvRow.class);

        Set<String> existingKeys = medicationRepo.findAll().stream()
                .map(m -> buildMedicationKey(
                        m.getPatient() != null ? m.getPatient().getId() : null,
                        m.getEncounter() != null ? m.getEncounter().getId() : null,
                        m.getCode(), m.getStartTime(), m.getDispenses()))
                .collect(Collectors.toCollection(HashSet::new));

        int count = 0;
        int pendingSinceFlush = 0;

        for (MedicationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime startTime = parseDateTime(r.getStart());
            Integer dispenses = parseInt(r.getDispenses());

            String key = buildMedicationKey(patient.getId(),
                    encounter != null ? encounter.getId() : null,
                    r.getCode(), startTime, dispenses);

            if (existingKeys.contains(key)) continue;
            existingKeys.add(key);

            EmrMedication medication = EmrMedication.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .code(r.getCode())
                    .startTime(startTime)
                    .dispenses(dispenses)
                    .stopTime(parseDateTime(r.getStop()))
                    .description(r.getDescription())
                    .baseCost(parseBigDecimal(r.getBaseCost()))
                    .payerCoverage(parseBigDecimal(r.getPayerCoverage()))
                    .totalCost(parseBigDecimal(r.getTotalCost()))
                    .reasonCode(r.getReasonCode())
                    .reasonDescription(r.getReasonDescription())
                    .build();

            medicationRepo.save(medication);
            count++;
            pendingSinceFlush++;

            if (pendingSinceFlush >= 1000) {
                entityManager.flush();
                pendingSinceFlush = 0;
            }
        }

        if (pendingSinceFlush > 0) {
            entityManager.flush();
        }

        return count;
    }

    /**
     * Builds a composite string key for in-memory duplicate detection of
     * medication rows. Null-safe: encounter and dispenses may be null.
     */
    private String buildMedicationKey(Long patientId, Long encounterId, String code,
                                      LocalDateTime startTime, Integer dispenses) {
        return patientId + "|" + encounterId + "|" + code + "|" + startTime + "|" + dispenses;
    }

    /**
     * Upserts procedures.csv into emr_procedure.
     * Natural key: composite (patient, encounter, code, procDate, baseCost).
     * See {@link #importObservations()} for the in-memory duplicate-check and
     * batched-flush performance rationale.
     *
     * @return number of rows newly inserted (exact duplicates are skipped)
     */
    public int importProcedures() {
        List<ProcedureCsvRow> rows = parseCsv("procedures.csv", ProcedureCsvRow.class);

        Set<String> existingKeys = procedureRepo.findAll().stream()
                .map(p -> buildProcedureKey(
                        p.getPatient() != null ? p.getPatient().getId() : null,
                        p.getEncounter() != null ? p.getEncounter().getId() : null,
                        p.getCode(), p.getProcDate(), p.getBaseCost()))
                .collect(Collectors.toCollection(HashSet::new));

        int count = 0;
        int pendingSinceFlush = 0;

        for (ProcedureCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime procDate = parseDateTime(r.getDate());
            BigDecimal baseCost = parseBigDecimal(r.getBaseCost());

            String key = buildProcedureKey(patient.getId(),
                    encounter != null ? encounter.getId() : null,
                    r.getCode(), procDate, baseCost);

            if (existingKeys.contains(key)) continue;
            existingKeys.add(key);

            EmrProcedure procedure = EmrProcedure.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .code(r.getCode())
                    .procDate(procDate)
                    .baseCost(baseCost)
                    .description(r.getDescription())
                    .reasonCode(r.getReasonCode())
                    .reasonDescription(r.getReasonDescription())
                    .build();

            procedureRepo.save(procedure);
            count++;
            pendingSinceFlush++;

            if (pendingSinceFlush >= 1000) {
                entityManager.flush();
                pendingSinceFlush = 0;
            }
        }

        if (pendingSinceFlush > 0) {
            entityManager.flush();
        }

        return count;
    }

    /**
     * Builds a composite string key for in-memory duplicate detection of
     * procedure rows. Null-safe: encounter and baseCost may be null.
     */
    private String buildProcedureKey(Long patientId, Long encounterId, String code,
                                     LocalDateTime procDate, BigDecimal baseCost) {
        return patientId + "|" + encounterId + "|" + code + "|" + procDate + "|" + baseCost;
    }

    /**
     * Upserts immunizations.csv into emr_immunization.
     * Natural key: composite (patient, encounter, code, immDate, baseCost).
     * See {@link #importObservations()} for the in-memory duplicate-check and
     * batched-flush performance rationale.
     *
     * @return number of rows newly inserted (exact duplicates are skipped)
     */
    public int importImmunizations() {
        List<ImmunizationCsvRow> rows = parseCsv("immunizations.csv", ImmunizationCsvRow.class);

        Set<String> existingKeys = immunizationRepo.findAll().stream()
                .map(i -> buildImmunizationKey(
                        i.getPatient() != null ? i.getPatient().getId() : null,
                        i.getEncounter() != null ? i.getEncounter().getId() : null,
                        i.getCode(), i.getImmDate(), i.getBaseCost()))
                .collect(Collectors.toCollection(HashSet::new));

        int count = 0;
        int pendingSinceFlush = 0;

        for (ImmunizationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime immDate = parseDateTime(r.getDate());
            BigDecimal baseCost = parseBigDecimal(r.getBaseCost());

            String key = buildImmunizationKey(patient.getId(),
                    encounter != null ? encounter.getId() : null,
                    r.getCode(), immDate, baseCost);

            if (existingKeys.contains(key)) continue;
            existingKeys.add(key);

            EmrImmunization immunization = EmrImmunization.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .code(r.getCode())
                    .immDate(immDate)
                    .baseCost(baseCost)
                    .description(r.getDescription())
                    .build();

            immunizationRepo.save(immunization);
            count++;
            pendingSinceFlush++;

            if (pendingSinceFlush >= 1000) {
                entityManager.flush();
                pendingSinceFlush = 0;
            }
        }

        if (pendingSinceFlush > 0) {
            entityManager.flush();
        }

        return count;
    }

    /**
     * Builds a composite string key for in-memory duplicate detection of
     * immunization rows. Null-safe: encounter and baseCost may be null.
     */
    private String buildImmunizationKey(Long patientId, Long encounterId, String code,
                                        LocalDateTime immDate, BigDecimal baseCost) {
        return patientId + "|" + encounterId + "|" + code + "|" + immDate + "|" + baseCost;
    }
    /**
     * Upserts allergies.csv into emr_allergy.
     * Natural key: composite (patient, encounter, code).
     * See {@link #importObservations()} for the in-memory duplicate-check and
     * batched-flush performance rationale.
     *
     * @return number of rows newly inserted (exact duplicates are skipped)
     */
    public int importAllergies() {
        List<AllergyCsvRow> rows = parseCsv("allergies.csv", AllergyCsvRow.class);

        Set<String> existingKeys = allergyRepo.findAll().stream()
                .map(a -> buildAllergyKey(
                        a.getPatient() != null ? a.getPatient().getId() : null,
                        a.getEncounter() != null ? a.getEncounter().getId() : null,
                        a.getCode()))
                .collect(Collectors.toCollection(HashSet::new));

        int count = 0;
        int pendingSinceFlush = 0;

        for (AllergyCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            String key = buildAllergyKey(patient.getId(),
                    encounter != null ? encounter.getId() : null,
                    r.getCode());

            if (existingKeys.contains(key)) continue;
            existingKeys.add(key);

            EmrAllergy allergy = EmrAllergy.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .code(r.getCode())
                    .startDate(parseDate(r.getStart()))
                    .stopDate(parseDate(r.getStop()))
                    .system(r.getSystem())
                    .description(r.getDescription())
                    .allergyType(r.getType())
                    .category(r.getCategory())
                    .reaction1Code(r.getReaction1Code())
                    .reaction1Desc(r.getReaction1Desc())
                    .reaction2Code(r.getReaction2Code())
                    .reaction2Desc(r.getReaction2Desc())
                    .build();

            allergyRepo.save(allergy);
            count++;
            pendingSinceFlush++;

            if (pendingSinceFlush >= 1000) {
                entityManager.flush();
                pendingSinceFlush = 0;
            }
        }

        if (pendingSinceFlush > 0) {
            entityManager.flush();
        }

        return count;
    }

    /**
     * Builds a composite string key for in-memory duplicate detection of
     * allergy rows. Null-safe: encounter may be null.
     */
    private String buildAllergyKey(Long patientId, Long encounterId, String code) {
        return patientId + "|" + encounterId + "|" + code;
    }


    /**
     * Upserts careplans.csv into emr_careplan.
     * Natural key: Synthea careplan Id.
     *
     * @return number of rows processed (inserted or updated)
     */
    public int importCareplans() {
        List<CareplanCsvRow> rows = parseCsv("careplans.csv", CareplanCsvRow.class);
        int count = 0;
        for (CareplanCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;

            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            EmrCareplan careplan = careplanRepo.findBySyntheaId(r.getId())
                    .orElse(EmrCareplan.builder().syntheaId(r.getId()).build());

            careplan.setPatient(patient);
            careplan.setEncounter(encounter);
            careplan.setStartDate(parseDate(r.getStart()));
            careplan.setStopDate(parseDate(r.getStop()));
            careplan.setCode(r.getCode());
            careplan.setDescription(r.getDescription());
            careplan.setReasonCode(r.getReasonCode());
            careplan.setReasonDescription(r.getReasonDescription());

            careplanRepo.save(careplan);
            count++;
        }
        return count;
    }

    // ── 공통 유틸 ──────────────────────────────────────

    /**
     * Parses a Synthea CSV file into a list of beans using OpenCSV.
     *
     * @param filename CSV file name relative to {@code csvImportPath}
     * @param type     target bean type to map CSV columns onto
     * @return parsed rows as a list of beans
     * @throws RuntimeException if the file cannot be found, read, or parsed
     */
    private <T> List<T> parseCsv(String filename, Class<T> type) {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                new ClassPathResource("csv/" + filename).getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            return new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

        } catch (Exception e) {
            throw new RuntimeException(filename + " 파싱 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a date-only string (e.g. "2020-01-15") into a LocalDate.
     *
     * @param v raw CSV value, possibly null/blank or longer (date-time) than 10 chars
     * @return parsed LocalDate, or null if the input is null/blank
     */
    private LocalDate parseDate(String v) {
        if (v == null || v.isBlank()) return null;
        return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
    }

    /**
     * Parses a Synthea timestamp string (e.g. "2020-01-15T10:30:00Z") into a LocalDateTime.
     *
     * @param v raw CSV value, possibly null/blank
     * @return parsed LocalDateTime with the trailing 'Z' stripped, or null if input is null/blank
     */
    private LocalDateTime parseDateTime(String v) {
        if (v == null || v.isBlank()) return null;
        // Synthea 형식: "2020-01-15T10:30:00Z"
        return LocalDateTime.parse(v.replace("Z", ""),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Parses a numeric string into a Double.
     *
     * @param v raw CSV value, possibly null/blank
     * @return parsed Double, or null if input is null/blank
     * @throws NumberFormatException if v is non-blank but not a valid double
     */
    private Double parseDouble(String v) {
        if (v == null || v.isBlank()) return null;
        return Double.parseDouble(v);
    }

    /**
     * Parses a numeric string into an Integer.
     *
     * @param v raw CSV value, possibly null/blank
     * @return parsed Integer, or null if input is null/blank
     * @throws NumberFormatException if v is non-blank but not a valid integer
     */
    private Integer parseInt(String v) {
        if (v == null || v.isBlank()) return null;
        return Integer.parseInt(v);
    }

    /**
     * Parses a numeric string into a BigDecimal.
     *
     * @param v raw CSV value, possibly null/blank
     * @return parsed BigDecimal, or null if input is null/blank
     * @throws NumberFormatException if v is non-blank but not a valid decimal
     */
    private BigDecimal parseBigDecimal(String v) {
        if (v == null || v.isBlank()) return null;
        return new BigDecimal(v);
    }
}