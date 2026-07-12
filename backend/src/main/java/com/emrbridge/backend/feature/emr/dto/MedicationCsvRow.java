package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicationCsvRow {
    @CsvBindByName(column = "START")            private String start;
    @CsvBindByName(column = "STOP")             private String stop;
    @CsvBindByName(column = "PATIENT")          private String patient;
    @CsvBindByName(column = "ENCOUNTER")        private String encounter;
    @CsvBindByName(column = "CODE")             private String code;
    @CsvBindByName(column = "DESCRIPTION")      private String description;
    @CsvBindByName(column = "BASE_COST")        private String baseCost;
    @CsvBindByName(column = "PAYER_COVERAGE")   private String payerCoverage;
    @CsvBindByName(column = "DISPENSES")        private String dispenses;
    @CsvBindByName(column = "TOTALCOST")        private String totalCost;
    @CsvBindByName(column = "REASONCODE")       private String reasonCode;
    @CsvBindByName(column = "REASONDESCRIPTION") private String reasonDescription;
}
