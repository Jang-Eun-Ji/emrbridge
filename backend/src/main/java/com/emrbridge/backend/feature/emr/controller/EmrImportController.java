package com.emrbridge.backend.feature.emr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.emrbridge.backend.feature.emr.service.SyntheaImportService;


import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emr/import")
public class EmrImportController {

//    private final EmrPatientImportService emrPatientImportService;
    private final SyntheaImportService syntheaImportService;

    // Endpoint to trigger the import of patients.csv
//    @PostMapping("/patients")
//    public ResponseEntity<Map<String, Object>> importPatients() {
//        int savedCount = emrPatientImportService.importPatients();
//
//        return ResponseEntity.ok(Map.of(
//                "message", "Synthea patients.csv import 완료",
//                "savedCount", savedCount
//        ));
//    }

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