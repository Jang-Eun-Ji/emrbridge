package com.emrbridge.backend.feature.analytics.controller;

import com.emrbridge.backend.feature.analytics.dto.AnalyticsEtlResult;
import com.emrbridge.backend.feature.analytics.dto.ConditionAnalysisDto;
import com.emrbridge.backend.feature.analytics.dto.DashboardSummaryDto;
import com.emrbridge.backend.feature.analytics.dto.PatientDetailDto;
import com.emrbridge.backend.feature.analytics.dto.PatientPageDto;
import com.emrbridge.backend.feature.analytics.dto.PipelineStatusDto;
import com.emrbridge.backend.feature.analytics.service.AnalyticsEtlService;
import com.emrbridge.backend.feature.analytics.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsEtlService analyticsEtlService;
    private final AnalyticsQueryService analyticsQueryService;

    /**
     * FHIR 서버에서 Patient/Encounter/Condition을 읽어 Analytics DB star schema에 적재합니다.
     */
    @PostMapping("/etl/run")
    public ResponseEntity<AnalyticsEtlResult> runEtl() {
        return ResponseEntity.ok(analyticsEtlService.runEtl());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDto> dashboard() {
        return ResponseEntity.ok(analyticsQueryService.getDashboard());
    }

    @GetMapping("/patients")
    public ResponseEntity<PatientPageDto> patients(
            @RequestParam(name = "q", required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(analyticsQueryService.listPatients(query));
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDetailDto> patient(@PathVariable("id") Integer id) {
        return analyticsQueryService.getPatient(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/conditions")
    public ResponseEntity<ConditionAnalysisDto> conditions() {
        return ResponseEntity.ok(analyticsQueryService.getConditions());
    }

    @GetMapping("/pipeline")
    public ResponseEntity<PipelineStatusDto> pipeline() {
        return ResponseEntity.ok(analyticsQueryService.getPipeline());
    }
}
