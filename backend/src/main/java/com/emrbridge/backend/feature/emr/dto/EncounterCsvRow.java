package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class EncounterCsvRow {
    @CsvBindByName(column = "Id")                   private String id;
    @CsvBindByName(column = "START")                private String start;
    @CsvBindByName(column = "STOP")                 private String stop;
    @CsvBindByName(column = "PATIENT")              private String patient;
    @CsvBindByName(column = "ORGANIZATION")         private String organization;
    @CsvBindByName(column = "PROVIDER")             private String provider;
    @CsvBindByName(column = "ENCOUNTERCLASS")       private String encounterClass;
    @CsvBindByName(column = "CODE")                 private String code;
    @CsvBindByName(column = "DESCRIPTION")          private String description;
    @CsvBindByName(column = "BASE_ENCOUNTER_COST")  private String baseEncounterCost;
    @CsvBindByName(column = "TOTAL_CLAIM_COST")     private String totalClaimCost;
    @CsvBindByName(column = "PAYER_COVERAGE")       private String payerCoverage;
    @CsvBindByName(column = "REASONCODE")           private String reasonCode;
    @CsvBindByName(column = "REASONDESCRIPTION")    private String reasonDescription;
}
