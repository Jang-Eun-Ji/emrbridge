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

import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String filePath = csvImportPath + "/patients.csv";

        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {

            CsvToBean<PatientCsvRow> csvToBean = new CsvToBeanBuilder<PatientCsvRow>(reader)
                    .withType(PatientCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<PatientCsvRow> rows = csvToBean.parse();

            int savedCount = 0;

            for (PatientCsvRow row : rows) {
                if (row.getId() == null || row.getId().isBlank()) {
                    continue;
                }

                EmrPatient patient = patientCache.getOrDefault(
                        row.getId(), EmrPatient.builder().syntheaPatientId(row.getId()).build());

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
                savedCount++;
            }

            return savedCount;

        } catch (Exception e) {
            throw new RuntimeException("patients.csv import failed: " + e.getMessage(), e);
        }
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
     * conditions.csv has no unique id column in the Synthea source, so the
     * natural key used here is the composite (patient, encounter, code, startDate).
     *
     * @return number of rows processed (inserted or updated)
     */
    public int importConditions() {
        List<ConditionCsvRow> rows = parseCsv("conditions.csv", ConditionCsvRow.class);
        int count = 0;
        for (ConditionCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDate startDate = parseDate(r.getStart());

            // Composite-key lookup: find an existing row that matches on
            // patient + encounter + code + startDate, or start a new one.
            EmrCondition condition = conditionRepo
                    .findByPatientAndEncounterAndCodeAndStartDate(patient, encounter, r.getCode(), startDate)
                    .orElse(EmrCondition.builder()
                            .patient(patient)
                            .encounter(encounter)
                            .code(r.getCode())
                            .startDate(startDate)
                            .build());

            condition.setStopDate(parseDate(r.getStop()));
            condition.setDescription(r.getDescription());

            conditionRepo.save(condition);
            count++;
        }
        return count;
    }

    /**
     * Upserts observations.csv into emr_observation.
     * Natural key: composite (patient, encounter, code, obsDate, value).
     * <p>
     * Uses findAllBy instead of findBy because the Synthea source can contain
     * multiple rows with identical values across every key field (verified in
     * production data - findBy previously threw NonUniqueResultException here).
     * If any row already matches on every key field, the CSV row is treated as
     * an exact duplicate and is skipped entirely (no update, no insert). Only
     * rows with no existing match are inserted as new.
     *
     * @return number of rows processed (newly inserted; exact duplicates are skipped)
     */
    public int importObservations() {
        List<ObservationCsvRow> rows = parseCsv("observations.csv", ObservationCsvRow.class);
        int count = 0;
        for (ObservationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime obsDate = parseDateTime(r.getDate());

            // If a row already exists with the exact same key values, this CSV
            // row is a duplicate of already-imported data - skip it entirely.
            List<EmrObservation> matches = observationRepo
                    .findAllByPatientAndEncounterAndCodeAndObsDateAndValue(
                            patient, encounter, r.getCode(), obsDate, r.getValue());
            if (!matches.isEmpty()) continue;

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
        }
        return count;
    }

    /**
     * Upserts medications.csv into emr_medication.
     * Natural key: composite (patient, encounter, code, startTime, dispenses).
     * Exact duplicates (all key fields matching an existing row) are skipped;
     * only genuinely new rows are inserted. See {@link #importObservations()}.
     *
     * @return number of rows processed (newly inserted; exact duplicates are skipped)
     */
    public int importMedications() {
        List<MedicationCsvRow> rows = parseCsv("medications.csv", MedicationCsvRow.class);
        int count = 0;
        for (MedicationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime startTime = parseDateTime(r.getStart());
            Integer dispenses = parseInt(r.getDispenses());

            List<EmrMedication> matches = medicationRepo
                    .findAllByPatientAndEncounterAndCodeAndStartTimeAndDispenses(
                            patient, encounter, r.getCode(), startTime, dispenses);
            if (!matches.isEmpty()) continue;

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
        }
        return count;
    }

    /**
     * Upserts procedures.csv into emr_procedure.
     * Natural key: composite (patient, encounter, code, procDate, baseCost).
     * Exact duplicates are skipped; only genuinely new rows are inserted.
     * See {@link #importObservations()}.
     *
     * @return number of rows processed (newly inserted; exact duplicates are skipped)
     */
    public int importProcedures() {
        List<ProcedureCsvRow> rows = parseCsv("procedures.csv", ProcedureCsvRow.class);
        int count = 0;
        for (ProcedureCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime procDate = parseDateTime(r.getDate());
            BigDecimal baseCost = parseBigDecimal(r.getBaseCost());

            List<EmrProcedure> matches = procedureRepo
                    .findAllByPatientAndEncounterAndCodeAndProcDateAndBaseCost(
                            patient, encounter, r.getCode(), procDate, baseCost);
            if (!matches.isEmpty()) continue;

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
        }
        return count;
    }

    /**
     * Upserts immunizations.csv into emr_immunization.
     * Natural key: composite (patient, encounter, code, immDate, baseCost).
     * Exact duplicates are skipped; only genuinely new rows are inserted.
     * See {@link #importObservations()}.
     *
     * @return number of rows processed (newly inserted; exact duplicates are skipped)
     */
    public int importImmunizations() {
        List<ImmunizationCsvRow> rows = parseCsv("immunizations.csv", ImmunizationCsvRow.class);
        int count = 0;
        for (ImmunizationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());
            LocalDateTime immDate = parseDateTime(r.getDate());
            BigDecimal baseCost = parseBigDecimal(r.getBaseCost());

            List<EmrImmunization> matches = immunizationRepo
                    .findAllByPatientAndEncounterAndCodeAndImmDateAndBaseCost(
                            patient, encounter, r.getCode(), immDate, baseCost);
            if (!matches.isEmpty()) continue;

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
        }
        return count;
    }
    /**
     * Upserts allergies.csv into emr_allergy.
     * Natural key: composite (patient, encounter, code) - allergies.csv has
     * no per-row date granular enough to disambiguate multiple rows, so code
     * within an encounter is used as the natural key.
     *
     * @return number of rows processed (inserted or updated)
     */
    public int importAllergies() {
        List<AllergyCsvRow> rows = parseCsv("allergies.csv", AllergyCsvRow.class);
        int count = 0;
        for (AllergyCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());
            if (patient == null) continue;

            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            EmrAllergy allergy = allergyRepo
                    .findByPatientAndEncounterAndCode(patient, encounter, r.getCode())
                    .orElse(EmrAllergy.builder()
                            .patient(patient)
                            .encounter(encounter)
                            .code(r.getCode())
                            .build());

            allergy.setStartDate(parseDate(r.getStart()));
            allergy.setStopDate(parseDate(r.getStop()));
            allergy.setSystem(r.getSystem());
            allergy.setDescription(r.getDescription());
            allergy.setAllergyType(r.getType());
            allergy.setCategory(r.getCategory());
            allergy.setReaction1Code(r.getReaction1Code());
            allergy.setReaction1Desc(r.getReaction1Desc());
            allergy.setReaction2Code(r.getReaction2Code());
            allergy.setReaction2Desc(r.getReaction2Desc());

            allergyRepo.save(allergy);
            count++;
        }
        return count;
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
        String path = csvImportPath + "/" + filename;
        try (FileReader reader = new FileReader(path, StandardCharsets.UTF_8)) {
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