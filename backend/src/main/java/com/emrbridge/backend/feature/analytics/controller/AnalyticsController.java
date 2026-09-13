package com.emrbridge.backend.feature.analytics.controller;

import com.emrbridge.backend.feature.analytics.dto.AnalyticsEtlResult;
import com.emrbridge.backend.feature.analytics.service.AnalyticsEtlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsEtlService analyticsEtlService;

    /**
     * FHIR 서버에서 Patient/Encounter/Condition을 읽어 Analytics DB star schema에 적재합니다.
     * Power BI 및 React 대시보드에서 사용할 집계 뷰(monthly_visit_summary 등)가 자동으로 갱신됩니다.
     */
    @PostMapping("/etl/run")
    public ResponseEntity<AnalyticsEtlResult> runEtl() {
        System.out.println("=== Running ETL ===");
        return ResponseEntity.ok(analyticsEtlService.runEtl());
    }
}
