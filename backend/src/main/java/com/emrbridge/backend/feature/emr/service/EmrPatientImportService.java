package com.emrbridge.backend.feature.emr.service;

import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import com.emrbridge.backend.feature.emr.dto.PatientCsvRow;
import com.emrbridge.backend.feature.emr.repository.EmrPatientRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmrPatientImportService {

    private final EmrPatientRepository emrPatientRepository;

    @Value("${csv.import.path}")
    private String csvImportPath;

    // read patients.csv and save to emr_patient table
    @Transactional
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

                if (emrPatientRepository.existsBySyntheaPatientId(row.getId())) {
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

                emrPatientRepository.save(patient);
                savedCount++;
            }

            return savedCount;

        } catch (Exception e) {
            throw new RuntimeException("patients.csv import failed: " + e.getMessage(), e);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value);
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }
}