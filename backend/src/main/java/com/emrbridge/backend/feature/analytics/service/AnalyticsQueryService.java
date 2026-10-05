package com.emrbridge.backend.feature.analytics.service;

import com.emrbridge.backend.feature.analytics.domain.DiseaseDim;
import com.emrbridge.backend.feature.analytics.domain.PatientDim;
import com.emrbridge.backend.feature.analytics.domain.RegionDim;
import com.emrbridge.backend.feature.analytics.domain.VisitFact;
import com.emrbridge.backend.feature.analytics.dto.CategorySummaryDto;
import com.emrbridge.backend.feature.analytics.dto.ConditionAnalysisDto;
import com.emrbridge.backend.feature.analytics.dto.DashboardSummaryDto;
import com.emrbridge.backend.feature.analytics.dto.DemographicSummaryDto;
import com.emrbridge.backend.feature.analytics.dto.DiseaseSummaryDto;
import com.emrbridge.backend.feature.analytics.dto.MonthlyVisitSummaryDto;
import com.emrbridge.backend.feature.analytics.dto.PatientDetailDto;
import com.emrbridge.backend.feature.analytics.dto.PatientPageDto;
import com.emrbridge.backend.feature.analytics.dto.PatientSummaryDto;
import com.emrbridge.backend.feature.analytics.dto.PatientVisitDto;
import com.emrbridge.backend.feature.analytics.dto.PipelineStageStatusDto;
import com.emrbridge.backend.feature.analytics.dto.PipelineStatusDto;
import com.emrbridge.backend.feature.analytics.dto.RegionSummaryDto;
import com.emrbridge.backend.feature.analytics.repository.DiseaseDimRepository;
import com.emrbridge.backend.feature.analytics.repository.PatientDimRepository;
import com.emrbridge.backend.feature.analytics.repository.RegionDimRepository;
import com.emrbridge.backend.feature.analytics.repository.VisitFactRepository;
import com.emrbridge.backend.feature.emr.repository.EmrPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private static final int DASHBOARD_RANK_LIMIT = 10;

    private final VisitFactRepository visitFactRepository;
    private final PatientDimRepository patientDimRepository;
    private final DiseaseDimRepository diseaseDimRepository;
    private final RegionDimRepository regionDimRepository;
    private final EmrPatientRepository emrPatientRepository;
    private final FhirExtractService fhirExtractService;

    @Transactional(transactionManager = "analyticsTransactionManager", readOnly = true)
    public DashboardSummaryDto getDashboard() {
        List<MonthlyVisitSummaryDto> monthlyVisits = visitFactRepository.findMonthlyVisitSummary().stream()
                .map(row -> new MonthlyVisitSummaryDto(
                        (String) row[0],
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).intValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();

        List<DemographicSummaryDto> demographics = visitFactRepository.findDemographicSummary().stream()
                .map(row -> new DemographicSummaryDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .toList();

        List<DiseaseSummaryDto> topDiseases = visitFactRepository
                .findDiseaseDistribution(PageRequest.of(0, DASHBOARD_RANK_LIMIT))
                .stream()
                .map(this::toDiseaseSummary)
                .toList();

        List<RegionSummaryDto> regions = visitFactRepository
                .findRegionDistribution(PageRequest.of(0, DASHBOARD_RANK_LIMIT))
                .stream()
                .map(row -> new RegionSummaryDto(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();

        return new DashboardSummaryDto(
                patientDimRepository.count(),
                visitFactRepository.sumVisitCount(),
                diseaseDimRepository.count(),
                regionDimRepository.count(),
                monthlyVisits,
                demographics,
                topDiseases,
                regions
        );
    }

    @Transactional(transactionManager = "analyticsTransactionManager", readOnly = true)
    public PatientPageDto listPatients(String query) {
        String q = query == null ? "" : query.trim();
        List<PatientSummaryDto> items = patientDimRepository.search(q).stream()
                .map(this::toPatientSummary)
                .toList();
        return new PatientPageDto(items, items.size());
    }

    @Transactional(transactionManager = "analyticsTransactionManager", readOnly = true)
    public Optional<PatientDetailDto> getPatient(Integer patientId) {
        return patientDimRepository.findByIdWithRegion(patientId).map(patient -> {
            RegionDim region = patient.getRegion();
            List<PatientVisitDto> visits = visitFactRepository.findByPatientIdWithDisease(patientId).stream()
                    .map(this::toPatientVisit)
                    .toList();
            return new PatientDetailDto(
                    patient.getPatientId(),
                    patient.getOriginalPatientId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getGender(),
                    patient.getBirthDate(),
                    patient.getAge(),
                    patient.getAgeGroup(),
                    region == null ? null : region.getRegionName(),
                    region == null ? null : region.getState(),
                    region == null ? null : region.getCity(),
                    patient.getRegistrationDate(),
                    visits
            );
        });
    }

    @Transactional(transactionManager = "analyticsTransactionManager", readOnly = true)
    public ConditionAnalysisDto getConditions() {
        List<DiseaseSummaryDto> diseases = visitFactRepository.findDiseaseDistribution().stream()
                .map(this::toDiseaseSummary)
                .toList();
        List<CategorySummaryDto> categories = visitFactRepository.findCategoryDistribution().stream()
                .map(row -> new CategorySummaryDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()
                ))
                .toList();
        return new ConditionAnalysisDto(categories, diseases);
    }

    public PipelineStatusDto getPipeline() {
        List<PipelineStageStatusDto> stages = new ArrayList<>();
        stages.add(emrStage());
        stages.add(fhirStage());
        stages.add(analyticsStage());
        return new PipelineStatusDto(stages);
    }

    private PipelineStageStatusDto emrStage() {
        try {
            long count = emrPatientRepository.count();
            return stage("emr", count, null, "EMR DB emr_patient 건수입니다.");
        } catch (Exception ex) {
            return new PipelineStageStatusDto("emr", "failed", null, null, ex.getMessage());
        }
    }

    private PipelineStageStatusDto fhirStage() {
        try {
            long count = fhirExtractService.countPatients();
            return stage("fhir", count, null, "HAPI FHIR Patient 리소스 건수입니다.");
        } catch (Exception ex) {
            return new PipelineStageStatusDto("fhir", "failed", null, null, ex.getMessage());
        }
    }

    private PipelineStageStatusDto analyticsStage() {
        try {
            long count = visitFactRepository.count();
            return stage("analytics", count, null, "Analytics visit_facts 건수입니다.");
        } catch (Exception ex) {
            return new PipelineStageStatusDto("analytics", "failed", null, null, ex.getMessage());
        }
    }

    private static PipelineStageStatusDto stage(String key, long count, java.time.LocalDateTime lastRunAt, String message) {
        String status = count > 0 ? "succeeded" : "idle";
        return new PipelineStageStatusDto(key, status, count, lastRunAt, message);
    }

    private DiseaseSummaryDto toDiseaseSummary(Object[] row) {
        return new DiseaseSummaryDto(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue()
        );
    }

    private PatientSummaryDto toPatientSummary(PatientDim patient) {
        RegionDim region = patient.getRegion();
        return new PatientSummaryDto(
                patient.getPatientId(),
                patient.getOriginalPatientId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getGender(),
                patient.getBirthDate(),
                patient.getAge(),
                patient.getAgeGroup(),
                region == null ? null : region.getRegionName()
        );
    }

    private PatientVisitDto toPatientVisit(VisitFact visit) {
        DiseaseDim disease = visit.getDisease();
        return new PatientVisitDto(
                visit.getVisitId(),
                visit.getVisitDate(),
                visit.getVisitType(),
                disease == null ? null : disease.getDiseaseCode(),
                disease == null ? null : disease.getDiseaseName(),
                disease == null ? null : disease.getDiseaseCategory()
        );
    }
}
