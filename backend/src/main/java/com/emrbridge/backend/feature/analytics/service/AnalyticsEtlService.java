package com.emrbridge.backend.feature.analytics.service;

import com.emrbridge.backend.feature.analytics.domain.*;
import com.emrbridge.backend.feature.analytics.dto.AnalyticsEtlResult;
import com.emrbridge.backend.feature.analytics.repository.*;
import com.emrbridge.backend.feature.analytics.util.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEtlService {

    private static final String ORG_IDENTIFIER_SYSTEM = "http://emrbridge.com/organization-id";
    private static final String UNKNOWN_HOSPITAL_KEY = "UNKNOWN";

    private final FhirExtractService fhirExtractService;
    private final VisitFactRepository visitFactRepository;
    private final RegionDimRepository regionDimRepository;
    private final PatientDimRepository patientDimRepository;
    private final DiseaseDimRepository diseaseDimRepository;
    private final TimeDimRepository timeDimRepository;
    private final HospitalDimRepository hospitalDimRepository;

    @Transactional(transactionManager = "analyticsTransactionManager")
    public AnalyticsEtlResult runEtl() {
        log.info("Analytics ETL 시작 (FHIR → Analytics DB)");

        List<Patient> fhirPatients = fhirExtractService.fetchAllPatients();
        List<Encounter> fhirEncounters = fhirExtractService.fetchAllEncounters();
        List<Condition> fhirConditions = fhirExtractService.fetchAllConditions();
        List<Organization> fhirOrganizations = fhirExtractService.fetchAllOrganizations();

        visitFactRepository.truncateAll();

        Map<String, RegionDim> regionByName = new HashMap<>();
        Map<Long, PatientDim> patientDimByFhirId = new HashMap<>();
        Map<String, DiseaseDim> diseaseByCode = new HashMap<>();
        Map<String, TimeDim> timeByYearMonth = new HashMap<>();
        Map<String, HospitalDim> hospitalByOrgKey = new HashMap<>();

        OrganizationIndex organizationIndex = indexOrganizations(fhirOrganizations);

        int skippedPatients = 0;
        int patientDimRows = 0;
        LocalDate today = LocalDate.now();

        for (Patient fhirPatient : fhirPatients) {
            Long fhirPatientId = fhirId(fhirPatient.getIdElement());
            if (fhirPatientId == null) {
                skippedPatients++;
                continue;
            }

            LocalDate birthDate = toLocalDate(fhirPatient.getBirthDateElement());
            if (birthDate == null) {
                skippedPatients++;
                continue;
            }

            String gender = AnalyticsMapper.toAnalyticsGender(fhirPatient.getGender());
            if (gender == null) {
                skippedPatients++;
                continue;
            }

            String state = firstNonBlank(extractAddressField(fhirPatient, Address::getState), "Unknown");
            String city = firstNonBlank(extractAddressField(fhirPatient, Address::getCity), "Unknown");
            String county = extractAddressField(fhirPatient, Address::getDistrict);

            RegionDim region = getOrCreateRegion(regionByName, state, county, city);

            String firstName = fhirPatient.getNameFirstRep().getGivenAsSingleString();
            String lastName = fhirPatient.getNameFirstRep().getFamily();

            int age = AnalyticsMapper.calculateAge(birthDate, today);
            String ageGroup = AnalyticsMapper.toAgeGroup(age);

            PatientDim patientDim = upsertPatient(
                    fhirPatientId, firstName, lastName, gender, birthDate, age, ageGroup, region
            );

            patientDimByFhirId.put(fhirPatientId, patientDim);
            patientDimRows++;
        }

        Map<Long, List<Condition>> conditionsByEncounterFhirId = groupConditionsByEncounterFhirId(fhirConditions);

        int skippedEncounters = 0;
        int visitFactRows = 0;
        Map<String, Boolean> firstVisitByPatientDisease = new HashMap<>();

        for (Encounter encounter : fhirEncounters) {
            Long fhirEncounterId = fhirId(encounter.getIdElement());
            if (fhirEncounterId == null) {
                skippedEncounters++;
                continue;
            }

            Long fhirPatientId = fhirIdFromReference(encounter.getSubject());
            if (fhirPatientId == null) {
                skippedEncounters++;
                continue;
            }

            PatientDim patientDim = patientDimByFhirId.get(fhirPatientId);
            if (patientDim == null) {
                skippedEncounters++;
                continue;
            }

            LocalDate visitDate = toLocalDate(encounter.getPeriod() != null ? encounter.getPeriod().getStartElement() : null);
            if (visitDate == null) {
                skippedEncounters++;
                continue;
            }

            RegionDim region = patientDim.getRegion();
            TimeDim timeDim = getOrCreateTimeDim(timeByYearMonth, visitDate);
            HospitalDim hospital = resolveHospital(hospitalByOrgKey, organizationIndex, encounter.getServiceProvider());

            List<DiseaseInfo> diseases = resolveDiseasesFromConditions(
                    conditionsByEncounterFhirId.get(fhirEncounterId)
            );
            if (diseases.isEmpty()) {
                skippedEncounters++;
                continue;
            }

            for (DiseaseInfo disease : diseases) {
                DiseaseDim diseaseDim = getOrCreateDisease(diseaseByCode, disease.code(), disease.name());

                String visitTypeKey = patientDim.getPatientId() + ":" + disease.code();
                String visitType = firstVisitByPatientDisease.putIfAbsent(visitTypeKey, Boolean.TRUE) == null
                        ? "first visit"
                        : "follow-up";

                visitFactRepository.save(VisitFact.builder()
                        .patient(patientDim)
                        .disease(diseaseDim)
                        .time(timeDim)
                        .region(region)
                        .hospital(hospital)
                        .visitDate(visitDate)
                        .visitCount(1)
                        .visitType(visitType)
                        .originalEncounterId(fhirEncounterId)
                        .build());

                visitFactRows++;
            }
        }

        log.info("Analytics ETL 완료 - patients: {}, encounters: {}, hospitals: {}, visit_facts: {}",
                patientDimRows, fhirEncounters.size() - skippedEncounters, hospitalByOrgKey.size(), visitFactRows);

        return AnalyticsEtlResult.builder()
                .patientsFromFhir(fhirPatients.size())
                .encountersFromFhir(fhirEncounters.size())
                .conditionsFromFhir(fhirConditions.size())
                .regionDimRows(regionByName.size())
                .patientDimRows(patientDimRows)
                .diseaseDimRows(diseaseByCode.size())
                .timeDimRows(timeByYearMonth.size())
                .hospitalDimRows(hospitalByOrgKey.size())
                .visitFactRows(visitFactRows)
                .skippedPatients(skippedPatients)
                .skippedEncounters(skippedEncounters)
                .build();
    }

    private PatientDim upsertPatient(
            Long fhirPatientId,
            String firstName,
            String lastName,
            String gender,
            LocalDate birthDate,
            int age,
            String ageGroup,
            RegionDim region
    ) {
        PatientDim patientDim = patientDimRepository.findByOriginalPatientId(fhirPatientId)
                .orElseGet(() -> PatientDim.builder().originalPatientId(fhirPatientId).build());

        patientDim.setFirstName(firstName);
        patientDim.setLastName(lastName);
        patientDim.setGender(gender);
        patientDim.setBirthDate(birthDate);
        patientDim.setAge(age);
        patientDim.setAgeGroup(ageGroup);
        patientDim.setRegion(region);
        patientDim.setRegistrationDate(birthDate);

        return patientDimRepository.save(patientDim);
    }

    private RegionDim getOrCreateRegion(Map<String, RegionDim> cache, String state, String county, String city) {
        String regionName = AnalyticsMapper.buildRegionName(state, city);
        return cache.computeIfAbsent(regionName, name ->
                regionDimRepository.findByRegionName(regionName)
                        .orElseGet(() -> regionDimRepository.save(RegionDim.builder()
                                .state(state)
                                .county(county)
                                .city(city)
                                .regionName(regionName)
                                .build()))
        );
    }

    private TimeDim getOrCreateTimeDim(Map<String, TimeDim> cache, LocalDate visitDate) {
        int year = visitDate.getYear();
        int month = visitDate.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        String yearMonth = String.format("%04d-%02d", year, month);

        return cache.computeIfAbsent(yearMonth, key ->
                timeDimRepository.findByYearMonth(yearMonth)
                        .orElseGet(() -> timeDimRepository.save(TimeDim.builder()
                                .year(year)
                                .month(month)
                                .quarter(quarter)
                                .yearMonth(yearMonth)
                                .build()))
        );
    }

    private DiseaseDim getOrCreateDisease(Map<String, DiseaseDim> cache, String code, String name) {
        return cache.computeIfAbsent(code, key ->
                diseaseDimRepository.findByDiseaseCode(code)
                        .orElseGet(() -> diseaseDimRepository.save(DiseaseDim.builder()
                                .diseaseCode(code)
                                .diseaseName(name != null && !name.isBlank() ? name : code)
                                .diseaseCategory(AnalyticsMapper.resolveDiseaseCategory(code, name))
                                .build()))
        );
    }

    private OrganizationIndex indexOrganizations(List<Organization> organizations) {
        Map<Long, Organization> byFhirId = new HashMap<>();
        Map<String, Organization> byOrgIdentifier = new HashMap<>();
        for (Organization organization : organizations) {
            Long id = fhirId(organization.getIdElement());
            if (id != null) {
                byFhirId.put(id, organization);
            }
            String orgKey = extractOrganizationIdentifier(organization);
            if (orgKey != null) {
                byOrgIdentifier.put(orgKey, organization);
            }
        }
        return new OrganizationIndex(byFhirId, byOrgIdentifier);
    }

    private HospitalDim resolveHospital(
            Map<String, HospitalDim> cache,
            OrganizationIndex organizationIndex,
            Reference serviceProvider
    ) {
        Organization organization = resolveOrganization(organizationIndex, serviceProvider);
        if (organization != null) {
            String orgKey = firstNonBlank(
                    extractOrganizationIdentifier(organization),
                    fhirId(organization.getIdElement()) != null
                            ? String.valueOf(fhirId(organization.getIdElement()))
                            : null,
                    UNKNOWN_HOSPITAL_KEY
            );
            String name = firstNonBlank(organization.getName(), serviceProviderDisplay(serviceProvider), orgKey);
            String city = organization.hasAddress() ? organization.getAddressFirstRep().getCity() : null;
            String state = organization.hasAddress() ? organization.getAddressFirstRep().getState() : null;
            return getOrCreateHospital(cache, orgKey, name, city, state);
        }

        String display = serviceProviderDisplay(serviceProvider);
        if (display != null) {
            return getOrCreateHospital(cache, "DISPLAY:" + display, display, null, null);
        }

        return getOrCreateHospital(cache, UNKNOWN_HOSPITAL_KEY, "Unknown Hospital", null, null);
    }

    private Organization resolveOrganization(OrganizationIndex index, Reference serviceProvider) {
        if (serviceProvider == null) {
            return null;
        }

        Long fhirOrgId = fhirIdFromReference(serviceProvider);
        if (fhirOrgId != null) {
            Organization byId = index.byFhirId().get(fhirOrgId);
            if (byId != null) {
                return byId;
            }
        }

        String identifierValue = extractIdentifierValueFromReference(serviceProvider, ORG_IDENTIFIER_SYSTEM);
        if (identifierValue != null) {
            return index.byOrgIdentifier().get(identifierValue);
        }

        return null;
    }

    private HospitalDim getOrCreateHospital(
            Map<String, HospitalDim> cache,
            String originalOrganizationId,
            String hospitalName,
            String city,
            String state
    ) {
        return cache.computeIfAbsent(originalOrganizationId, key ->
                hospitalDimRepository.findByOriginalOrganizationId(originalOrganizationId)
                        .orElseGet(() -> hospitalDimRepository.save(HospitalDim.builder()
                                .originalOrganizationId(originalOrganizationId)
                                .hospitalName(hospitalName != null && !hospitalName.isBlank()
                                        ? hospitalName
                                        : originalOrganizationId)
                                .city(city)
                                .state(state)
                                .build()))
        );
    }

    private String extractOrganizationIdentifier(Organization organization) {
        if (organization == null || !organization.hasIdentifier()) {
            return null;
        }
        for (Identifier identifier : organization.getIdentifier()) {
            if (ORG_IDENTIFIER_SYSTEM.equals(identifier.getSystem())
                    && identifier.getValue() != null
                    && !identifier.getValue().isBlank()) {
                return identifier.getValue();
            }
        }
        // Fallback: first non-blank identifier value
        for (Identifier identifier : organization.getIdentifier()) {
            if (identifier.getValue() != null && !identifier.getValue().isBlank()) {
                return identifier.getValue();
            }
        }
        return null;
    }

    /**
     * Parses identifier search refs such as
     * Organization?identifier=http://emrbridge.com/organization-id|uuid
     * or Organization?identifier=uuid
     */
    private String extractIdentifierValueFromReference(Reference reference, String preferredSystem) {
        if (reference == null || reference.getReference() == null) {
            return null;
        }
        String ref = reference.getReference();
        int marker = ref.indexOf("identifier=");
        if (marker < 0) {
            return null;
        }
        String raw = ref.substring(marker + "identifier=".length());
        int amp = raw.indexOf('&');
        if (amp >= 0) {
            raw = raw.substring(0, amp);
        }
        raw = raw.trim();
        int pipe = raw.indexOf('|');
        if (pipe >= 0) {
            String system = raw.substring(0, pipe);
            String value = raw.substring(pipe + 1);
            if (preferredSystem == null || preferredSystem.equals(system)) {
                return value.isBlank() ? null : value;
            }
            return null;
        }
        return raw.isBlank() ? null : raw;
    }

    private String serviceProviderDisplay(Reference serviceProvider) {
        if (serviceProvider == null || !serviceProvider.hasDisplay()) {
            return null;
        }
        String display = serviceProvider.getDisplay();
        return display != null && !display.isBlank() ? display : null;
    }

    private Map<Long, List<Condition>> groupConditionsByEncounterFhirId(List<Condition> conditions) {
        Map<Long, List<Condition>> grouped = new HashMap<>();
        for (Condition condition : conditions) {
            Long encounterFhirId = fhirIdFromReference(condition.getEncounter());
            if (encounterFhirId != null) {
                grouped.computeIfAbsent(encounterFhirId, key -> new ArrayList<>()).add(condition);
            }
        }
        return grouped;
    }

    /** Disease names/codes come only from FHIR Condition.code (no Encounter reason/type fallback). */
    private List<DiseaseInfo> resolveDiseasesFromConditions(List<Condition> linkedConditions) {
        List<DiseaseInfo> diseases = new ArrayList<>();
        if (linkedConditions == null) {
            return diseases;
        }
        for (Condition condition : linkedConditions) {
            Coding coding = firstCoding(condition.getCode());
            if (coding != null && coding.hasCode() && !coding.getCode().isBlank()) {
                diseases.add(new DiseaseInfo(coding.getCode(), coding.getDisplay()));
            }
        }
        return diseases.stream().distinct().toList();
    }

    private Coding firstCoding(CodeableConcept concept) {
        if (concept == null || !concept.hasCoding()) {
            return null;
        }
        return concept.getCodingFirstRep();
    }

    private Long fhirId(IIdType id) {
        if (id == null || !id.hasIdPart()) {
            return null;
        }
        return parseFhirId(id.getIdPart());
    }

    private Long fhirIdFromReference(Reference reference) {
        if (reference == null || reference.getReferenceElement() == null) {
            return null;
        }
        return fhirId(reference.getReferenceElement());
    }

    private Long parseFhirId(String idPart) {
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            log.warn("Non-numeric FHIR id skipped: {}", idPart);
            return null;
        }
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

    private record OrganizationIndex(
            Map<Long, Organization> byFhirId,
            Map<String, Organization> byOrgIdentifier
    ) {
    }
}
