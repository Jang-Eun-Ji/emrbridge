package com.emrbridge.backend.feature.emr.controller;

import com.emrbridge.backend.feature.fhir.service.FhirUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.emrbridge.backend.feature.emr.service.SyntheaImportService;


import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emr/import")
public class EmrImportController {

    private final SyntheaImportService syntheaImportService;

    @PostMapping("/import/all")
    public ResponseEntity<Map<String, Integer>> importAll() {
        System.out.println("=== Synthea CSV Import 시작 ===");
        Map<String, Integer> result = syntheaImportService.importAll();
//        log.info("=== Synthea CSV Import 완료 ===");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/encounters")
    public ResponseEntity<Integer> importEncounters() {
        return ResponseEntity.ok(syntheaImportService.importEncounters());
    }

}