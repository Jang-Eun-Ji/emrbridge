package com.emrbridge.backend.feature.analytics.service;

import com.emrbridge.backend.feature.analytics.dto.AnalyticsEtlResult;
import com.emrbridge.backend.feature.analytics.repository.*;
import com.emrbridge.backend.feature.analytics.util.AnalyticsMapper;
import com.emrbridge.backend.feature.analytics.util.FhirIdentifierResolver;
import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import com.emrbridge.backend.feature.emr.repository.EmrEncounterRepository;
import com.emrbridge.backend.feature.emr.repository.EmrPatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEtlService {

    private final FhirExtractService fhirExtractService;
    private final EmrPatientRepository emrPatientRepository;
    private final EmrEncounterRepository emrEncounterRepository;
    private final AnalyticsEtlRepository analyticsEtlRepository;
    private final RegionDimRepository regionDimRepository;
    private final PatientDimRepository patientDimRepository;
    private final DiseaseDimRepository diseaseDimRepository;
    private final TimeDimRepository timeDimRepository;
    private final VisitFactRepository visitFactRepository;

    public AnalyticsEtlResult runEtl() {
        log.info("Analytics ETL 시작 (FHIR → Analytics DB)");

        List<Patient> fhirPatients = fhirExtractService.fetchAllPatients();
        List<Encounter> fhirEncounters = fhirExtractService.fetchAllEncounters();
        List<Condition> fhirConditions = fhirExtractService.fetchAllConditions();

        FhirIdentifierResolver identifierResolver = new FhirIdentifierResolver(fhirPatients, fhirEncounters);

        Map<String, Long> emrPatientIdBySyntheaId = emrPatientRepository.findAll().stream()
                .collect(Collectors.toMap(EmrPatient::getSyntheaPatientId, EmrPatient::getId));
        Map<String, EmrPatient> emrPatientBySyntheaId = emrPatientRepository.findAll().stream()
                .collect(Collectors.toMap(EmrPatient::getSyntheaPatientId, p -> p));
        Map<String, Long> emrEncounterIdBySyntheaId = emrEncounterRepository.findAll().stream()
                .collect(Collectors.toMap(EmrEncounter::getSyntheaId, EmrEncounter::getId));

        analyticsEtlRepository.truncateAll();

        Map<String, Integer> regionIdByName = new HashMap<>();
        Map<Long, Integer> patientDimIdByEmrId = new HashMap<>();
        Map<String, Integer> diseaseIdByCode = new HashMap<>();
        Map<String, Integer> timeIdByYearMonth = new HashMap<>();

        int skippedPatients = 0;
        int patientDimRows = 0;
        LocalDate today = LocalDate.now();

        for (Patient fhirPatient : fhirPatients) {
            String syntheaPatientId = identifierResolver.extractPatientSyntheaId(fhirPatient);
            if (syntheaPatientId == null) {
                skippedPatients++;
                continue;
            }

            Long emrPatientId = emrPatientIdBySyntheaId.get(syntheaPatientId);
            if (emrPatientId == null) {
                log.warn("EMR patient not found for synthea id: {}", syntheaPatientId);
                skippedPatients++;
                continue;
            }

            EmrPatient emrPatient = emrPatientBySyntheaId.get(syntheaPatientId);

            LocalDate birthDate = toLocalDate(fhirPatient.getBirthDateElement());
            if (birthDate == null && emrPatient != null) {
                birthDate = emrPatient.getBirthDate();
            }
            if (birthDate == null) {
                skippedPatients++;
                continue;
            }

            String gender = AnalyticsMapper.toAnalyticsGender(fhirPatient.getGender());
            if (gender == null && emrPatient != null && emrPatient.getGender() != null) {
                gender = switch (emrPatient.getGender().toUpperCase()) {
                    case "M", "MALE" -> "M";
                    case "F", "FEMALE" -> "F";
                    default -> null;
                };
            }
            if (gender == null) {
                skippedPatients++;
                continue;
            }

            String state = firstNonBlank(extractAddressField(fhirPatient, Address::getState), emrPatient != null ? emrPatient.getState() : null, "Unknown");
            String city = firstNonBlank(extractAddressField(fhirPatient, Address::getCity), emrPatient != null ? emrPatient.getCity() : null, "Unknown");
            String county = emrPatient != null ? emrPatient.getCounty() : null;

            int regionId = getOrCreateRegion(regionIdByName, state, county, city);

            String firstName = fhirPatient.getNameFirstRep().getGivenAsSingleString();
            String lastName = fhirPatient.getNameFirstRep().getFamily();
            if ((firstName == null || firstName.isBlank()) && emrPatient != null) {
                firstName = emrPatient.getFirstName();
            }
            if ((lastName == null || lastName.isBlank()) && emrPatient != null) {
                lastName = emrPatient.getLastName();
            }

            int age = AnalyticsMapper.calculateAge(birthDate, today);
            String ageGroup = AnalyticsMapper.toAgeGroup(age);

            int patientDimId = patientDimRepository.upsert(
                    emrPatientId, firstName, lastName, gender, birthDate, age, ageGroup, regionId
            );

            patientDimIdByEmrId.put(emrPatientId, patientDimId);
            patientDimRows++;
        }

        Map<String, List<Condition>> conditionsByEncounterSyntheaId =
                identifierResolver.groupConditionsByEncounterSyntheaId(fhirConditions);

        int skippedEncounters = 0;
        int visitFactRows = 0;
        Map<String, Boolean> firstVisitByPatientDisease = new HashMap<>();

        for (Encounter encounter : fhirEncounters) {
            String encounterSyntheaId = identifierResolver.extractEncounterSyntheaId(encounter);
            if (encounterSyntheaId == null) {
                skippedEncounters++;
                continue;
            }

            String patientSyntheaId = identifierResolver.resolvePatientSyntheaId(encounter.getSubject());
            if (patientSyntheaId == null) {
                skippedEncounters++;
                continue;
            }

            Long emrPatientId = emrPatientIdBySyntheaId.get(patientSyntheaId);
            Integer patientDimId = emrPatientId != null ? patientDimIdByEmrId.get(emrPatientId) : null;
            if (patientDimId == null) {
                skippedEncounters++;
                continue;
            }

            LocalDate visitDate = toLocalDate(encounter.getPeriod() != null ? encounter.getPeriod().getStartElement() : null);
            if (visitDate == null) {
                skippedEncounters++;
                continue;
            }

            EmrPatient emrPatient = emrPatientBySyntheaId.get(patientSyntheaId);
            String state = emrPatient != null && emrPatient.getState() != null ? emrPatient.getState() : "Unknown";
            String city = emrPatient != null && emrPatient.getCity() != null ? emrPatient.getCity() : "Unknown";
            String county = emrPatient != null ? emrPatient.getCounty() : null;
            int regionId = getOrCreateRegion(regionIdByName, state, county, city);
            int timeId = getOrCreateTimeDim(timeIdByYearMonth, visitDate);
            Long emrEncounterId = emrEncounterIdBySyntheaId.get(encounterSyntheaId);

            List<DiseaseInfo> diseases = resolveDiseases(encounter, conditionsByEncounterSyntheaId.get(encounterSyntheaId));
            if (diseases.isEmpty()) {
                diseases = List.of(new DiseaseInfo(
                        AnalyticsMapper.unknownDiseaseCode(),
                        AnalyticsMapper.unknownDiseaseName()
                ));
            }

            for (DiseaseInfo disease : diseases) {
                int diseaseId = getOrCreateDisease(diseaseIdByCode, disease.code(), disease.name());

                String visitTypeKey = patientDimId + ":" + disease.code();
                String visitType = firstVisitByPatientDisease.putIfAbsent(visitTypeKey, Boolean.TRUE) == null
                        ? "first visit"
                        : "follow-up";

                visitFactRepository.insert(
                        patientDimId, diseaseId, timeId, regionId, visitDate, visitType, emrEncounterId
                );
                visitFactRows++;
            }
        }

        log.info("Analytics ETL 완료 - patients: {}, encounters: {}, visit_facts: {}",
                patientDimRows, fhirEncounters.size() - skippedEncounters, visitFactRows);

        return AnalyticsEtlResult.builder()
                .patientsFromFhir(fhirPatients.size())
                .encountersFromFhir(fhirEncounters.size())
                .conditionsFromFhir(fhirConditions.size())
                .regionDimRows(regionIdByName.size())
                .patientDimRows(patientDimRows)
                .diseaseDimRows(diseaseIdByCode.size())
                .timeDimRows(timeIdByYearMonth.size())
                .visitFactRows(visitFactRows)
                .skippedPatients(skippedPatients)
                .skippedEncounters(skippedEncounters)
                .build();
    }

    private int getOrCreateRegion(Map<String, Integer> cache, String state, String county, String city) {
        String regionName = AnalyticsMapper.buildRegionName(state, city);
        return cache.computeIfAbsent(regionName, name ->
                regionDimRepository.upsert(state, county, city, regionName));
    }

    private int getOrCreateTimeDim(Map<String, Integer> cache, LocalDate visitDate) {
        int year = visitDate.getYear();
        int month = visitDate.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        String yearMonth = String.format("%04d-%02d", year, month);

        return cache.computeIfAbsent(yearMonth, key ->
                timeDimRepository.upsert(year, month, quarter, yearMonth));
    }

    private int getOrCreateDisease(Map<String, Integer> cache, String code, String name) {
        String diseaseCode = code != null && !code.isBlank() ? code : AnalyticsMapper.unknownDiseaseCode();
        String diseaseName = name != null && !name.isBlank() ? name : AnalyticsMapper.unknownDiseaseName();
        String category = AnalyticsMapper.resolveDiseaseCategory(diseaseName);

        return cache.computeIfAbsent(diseaseCode, key ->
                diseaseDimRepository.upsert(diseaseCode, diseaseName, category));
    }

    private List<DiseaseInfo> resolveDiseases(Encounter encounter, List<Condition> linkedConditions) {
        List<DiseaseInfo> diseases = new ArrayList<>();

        if (linkedConditions != null) {
            for (Condition condition : linkedConditions) {
                Coding coding = firstCoding(condition.getCode());
                if (coding != null) {
                    diseases.add(new DiseaseInfo(coding.getCode(), coding.getDisplay()));
                }
            }
        }

        if (diseases.isEmpty() && encounter.hasReasonCode()) {
            for (CodeableConcept reason : encounter.getReasonCode()) {
                Coding coding = firstCoding(reason);
                if (coding != null) {
                    diseases.add(new DiseaseInfo(coding.getCode(), coding.getDisplay()));
                }
            }
        }

        if (diseases.isEmpty() && encounter.hasType()) {
            for (CodeableConcept type : encounter.getType()) {
                Coding coding = firstCoding(type);
                if (coding != null) {
                    diseases.add(new DiseaseInfo(coding.getCode(), coding.getDisplay()));
                }
            }
        }

        return diseases.stream()
                .filter(d -> d.code() != null && !d.code().isBlank())
                .distinct()
                .toList();
    }

    private Coding firstCoding(CodeableConcept concept) {
        if (concept == null || !concept.hasCoding()) {
            return null;
        }
        return concept.getCodingFirstRep();
    }

    private LocalDate toLocalDate(DateTimeType dateTimeType) {
        if (dateTimeType == null || dateTimeType.getValue() == null) {
            return null;
        }
        return dateTimeType.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private LocalDate toLocalDate(DateType dateType) {
        if (dateType == null || dateType.getValue() == null) {
            return null;
        }
        return dateType.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private String extractAddressField(Patient patient, java.util.function.Function<Address, String> extractor) {
        if (!patient.hasAddress()) {
            return null;
        }
        return extractor.apply(patient.getAddressFirstRep());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record DiseaseInfo(String code, String name) {
    }
}
