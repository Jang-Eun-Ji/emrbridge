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

    private Map<String, EmrPatient>      patientCache      = new HashMap<>();
    private Map<String, EmrEncounter>    encounterCache    = new HashMap<>();
    private Map<String, EmrOrganization> organizationCache = new HashMap<>();
    private Map<String, EmrProvider>     providerCache     = new HashMap<>();
    // 전체 임포트 (순서 엄수)
    @Transactional
    public Map<String, Integer> importAll() {
        // 먼저 캐시 로딩
        patientCache = patientRepo.findAll().stream()
                .collect(Collectors.toMap(EmrPatient::getSyntheaPatientId, p -> p));
        organizationCache = orgRepo.findAll().stream()
                .collect(Collectors.toMap(EmrOrganization::getSyntheaId, o -> o));
        providerCache = providerRepo.findAll().stream()
                .collect(Collectors.toMap(EmrProvider::getSyntheaId, p -> p));
        encounterCache = new HashMap<>(); // encounter는 import 후 채움

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

    public int importOrganizations() {
        List<OrganizationCsvRow> rows = parseCsv("organizations.csv", OrganizationCsvRow.class);
        int count = 0;
        for (OrganizationCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;
            if (organizationCache.containsKey(r.getId())) continue;

            EmrOrganization org = EmrOrganization.builder()
                    .syntheaId(r.getId())
                    .name(r.getName())
                    .address(r.getAddress())
                    .city(r.getCity())
                    .state(r.getState())
                    .zip(r.getZip())
                    .lat(parseDouble(r.getLat()))
                    .lon(parseDouble(r.getLon()))
                    .phone(r.getPhone())
                    .build();

            orgRepo.save(org);
            organizationCache.put(r.getId(), org);
            count++;
        }
        return count;
    }

    public int importProviders() {
        List<ProviderCsvRow> rows = parseCsv("providers.csv", ProviderCsvRow.class);
        int count = 0;
        for (ProviderCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;
            if (providerCache.containsKey(r.getId())) continue;

            EmrOrganization org = organizationCache.get(r.getOrganization());

            EmrProvider provider = EmrProvider.builder()
                    .syntheaId(r.getId())
                    .organization(org)
                    .name(r.getName())
                    .gender(r.getGender())
                    .speciality(r.getSpeciality())
                    .address(r.getAddress())
                    .city(r.getCity())
                    .state(r.getState())
                    .zip(r.getZip())
                    .lat(parseDouble(r.getLat()))
                    .lon(parseDouble(r.getLon()))
                    .utilization(parseInt(r.getUtilization()))
                    .build();

            providerRepo.save(provider);
            providerCache.put(r.getId(), provider);
            count++;
        }
        return count;
    }

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

                if (patientCache.containsKey(row.getId())) {
                    continue;
                }

                EmrPatient patient = EmrPatient.builder()
                        .syntheaPatientId(row.getId())
                        .firstName(row.getFirst())
                        .lastName(row.getLast())
                        .gender(row.getGender())
                        .birthDate(parseDate(row.getBirthDate()))
                        .deathDate(parseDate(row.getDeathDate()))
                        .race(row.getRace())
                        .ethnicity(row.getEthnicity())
                        .address(row.getAddress())
                        .city(row.getCity())
                        .state(row.getState())
                        .county(row.getCounty())
                        .zip(row.getZip())
                        .lat(parseDouble(row.getLat()))
                        .lon(parseDouble(row.getLon()))
                        .healthcareExpenses(parseBigDecimal(row.getHealthcareExpenses()))
                        .healthcareCoverage(parseBigDecimal(row.getHealthcareCoverage()))
                        .build();

                patientRepo.save(patient);
                patientCache.put(row.getId(), patient); // save it cache after saving to the database
                savedCount++;
            }

            return savedCount;

        } catch (Exception e) {
            throw new RuntimeException("patients.csv import failed: " + e.getMessage(), e);
        }
    }

    public int importEncounters() {
        List<EncounterCsvRow> rows = parseCsv("encounters.csv", EncounterCsvRow.class);
        int count = 0;
        for (EncounterCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;
            if (encounterCache.containsKey(r.getId())) continue;

            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue; // patient 없으면 skip

            EmrOrganization org      = organizationCache.get(r.getOrganization());
            EmrProvider     provider = providerCache.get(r.getProvider());

            EmrEncounter encounter = EmrEncounter.builder()
                    .syntheaId(r.getId())
                    .patient(patient)
                    .organization(org)
                    .provider(provider)
                    .startTime(parseDateTime(r.getStart()))
                    .stopTime(parseDateTime(r.getStop()))
                    .encounterClass(r.getEncounterClass())
                    .code(r.getCode())
                    .description(r.getDescription())
                    .baseEncounterCost(parseBigDecimal(r.getBaseEncounterCost()))
                    .totalClaimCost(parseBigDecimal(r.getTotalClaimCost()))
                    .payerCoverage(parseBigDecimal(r.getPayerCoverage()))
                    .reasonCode(r.getReasonCode())
                    .reasonDescription(r.getReasonDescription())
                    .build();

            encounterRepo.save(encounter);
            encounterCache.put(r.getId(), encounter); // ✅ 추가
            count++;
        }
        return count;
    }

    public int importConditions() {
        List<ConditionCsvRow> rows = parseCsv("conditions.csv", ConditionCsvRow.class);
        int count = 0;
        for (ConditionCsvRow r : rows) {
            EmrPatient  patient  = patientRepo.findBySyntheaPatientId(r.getPatient()).orElse(null);
            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            conditionRepo.save(EmrCondition.builder()
                    .patient(patient).encounter(encounter)
                    .startDate(parseDate(r.getStart()))
                    .stopDate(parseDate(r.getStop()))
                    .code(r.getCode()).description(r.getDescription())
                    .build());
            count++;
        }
        return count;
    }

    // observations, medications, procedures, allergies, immunizations, careplans
    // 동일한 패턴이므로 생략 없이 구현하세요 (patient/encounter 조회 후 save)

    public int importObservations() {
        List<ObservationCsvRow> rows = parseCsv("observations.csv", ObservationCsvRow.class);
        int count = 0;
        for (ObservationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            observationRepo.save(EmrObservation.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .obsDate(parseDateTime(r.getDate()))
                    .category(r.getCategory())
                    .code(r.getCode())
                    .description(r.getDescription())
                    .value(r.getValue())
                    .units(r.getUnits())
                    .obsType(r.getType())
                    .build());
            count++;
        }
        return count;
    }

    public int importMedications() {
        List<MedicationCsvRow> rows = parseCsv("medications.csv", MedicationCsvRow.class);
        int count = 0;
        for (MedicationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            medicationRepo.save(EmrMedication.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .startTime(parseDateTime(r.getStart()))
                    .stopTime(parseDateTime(r.getStop()))
                    .code(r.getCode())
                    .description(r.getDescription())
                    .baseCost(parseBigDecimal(r.getBaseCost()))
                    .payerCoverage(parseBigDecimal(r.getPayerCoverage()))
                    .dispenses(parseInt(r.getDispenses()))
                    .totalCost(parseBigDecimal(r.getTotalCost()))
                    .reasonCode(r.getReasonCode())
                    .reasonDescription(r.getReasonDescription())
                    .build());
            count++;
        }
        return count;
    }

    public int importProcedures() {
        List<ProcedureCsvRow> rows = parseCsv("procedures.csv", ProcedureCsvRow.class);
        int count = 0;
        for (ProcedureCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            procedureRepo.save(EmrProcedure.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .procDate(parseDateTime(r.getDate()))
                    .code(r.getCode())
                    .description(r.getDescription())
                    .baseCost(parseBigDecimal(r.getBaseCost()))
                    .reasonCode(r.getReasonCode())
                    .reasonDescription(r.getReasonDescription())
                    .build());
            count++;
        }
        return count;
    }

    public int importAllergies() {
        List<AllergyCsvRow> rows = parseCsv("allergies.csv", AllergyCsvRow.class);
        int count = 0;
        for (AllergyCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            allergyRepo.save(EmrAllergy.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .startDate(parseDate(r.getStart()))
                    .stopDate(parseDate(r.getStop()))
                    .code(r.getCode())
                    .system(r.getSystem())
                    .description(r.getDescription())
                    .allergyType(r.getType())
                    .category(r.getCategory())
                    .reaction1Code(r.getReaction1Code())
                    .reaction1Desc(r.getReaction1Desc())
                    .reaction2Code(r.getReaction2Code())
                    .reaction2Desc(r.getReaction2Desc())
                    .build());
            count++;
        }
        return count;
    }

    public int importImmunizations() {
        List<ImmunizationCsvRow> rows = parseCsv("immunizations.csv", ImmunizationCsvRow.class);
        int count = 0;
        for (ImmunizationCsvRow r : rows) {
            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            immunizationRepo.save(EmrImmunization.builder()
                    .patient(patient)
                    .encounter(encounter)
                    .immDate(parseDateTime(r.getDate()))
                    .code(r.getCode())
                    .description(r.getDescription())
                    .baseCost(parseBigDecimal(r.getBaseCost()))
                    .build());
            count++;
        }
        return count;
    }

    public int importCareplans() {
        List<CareplanCsvRow> rows = parseCsv("careplans.csv", CareplanCsvRow.class);
        int count = 0;
        for (CareplanCsvRow r : rows) {
            if (r.getId() == null || r.getId().isBlank()) continue;
            if (careplanRepo.existsBySyntheaId(r.getId())) continue;

            EmrPatient patient = patientCache.get(r.getPatient());

            if (patient == null) continue;
            EmrEncounter encounter = encounterCache.get(r.getEncounter());

            careplanRepo.save(EmrCareplan.builder()
                    .syntheaId(r.getId())
                    .patient(patient)
                    .encounter(encounter)
                    .startDate(parseDate(r.getStart()))
                    .stopDate(parseDate(r.getStop()))
                    .code(r.getCode())
                    .description(r.getDescription())
                    .reasonCode(r.getReasonCode())
                    .reasonDescription(r.getReasonDescription())
                    .build());
            count++;
        }
        return count;
    }


    // ── 공통 유틸 ──────────────────────────────────────
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

    private LocalDate parseDate(String v) {
        if (v == null || v.isBlank()) return null;
        return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
    }

    private LocalDateTime parseDateTime(String v) {
        if (v == null || v.isBlank()) return null;
        // Synthea 형식: "2020-01-15T10:30:00Z"
        return LocalDateTime.parse(v.replace("Z", ""),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private Double parseDouble(String v) {
        if (v == null || v.isBlank()) return null;
        return Double.parseDouble(v);
    }

    private Integer parseInt(String v) {
        if (v == null || v.isBlank()) return null;
        return Integer.parseInt(v);
    }

    private BigDecimal parseBigDecimal(String v) {
        if (v == null || v.isBlank()) return null;
        return new BigDecimal(v);
    }
}