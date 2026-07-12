package com.emrbridge.backend.feature.fhir.controller;

import com.emrbridge.backend.feature.fhir.service.FhirUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fhir")
public class FhirController {

    private final FhirUploadService fhirUploadService;

    @PostMapping("/upload/patients")
    public ResponseEntity<Map<String, Integer>> uploadPatients() {
        return ResponseEntity.ok(Map.of("uploaded", fhirUploadService.uploadAllPatients()));
    }

    @PostMapping("/upload/all")
    public ResponseEntity<Map<String, Integer>> uploadAll() {
        return ResponseEntity.ok(fhirUploadService.uploadAll());
    }
}