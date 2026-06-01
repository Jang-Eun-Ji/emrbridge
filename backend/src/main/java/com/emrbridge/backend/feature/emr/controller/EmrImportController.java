package com.emrbridge.backend.feature.emr.controller;

import com.emrbridge.backend.feature.emr.service.EmrPatientImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emr/import")
public class EmrImportController {

    private final EmrPatientImportService emrPatientImportService;

    @PostMapping("/patients")
    public ResponseEntity<Map<String, Object>> importPatients() {
        int savedCount = emrPatientImportService.importPatients();

        return ResponseEntity.ok(Map.of(
                "message", "Synthea patients.csv import 완료",
                "savedCount", savedCount
        ));
    }
}