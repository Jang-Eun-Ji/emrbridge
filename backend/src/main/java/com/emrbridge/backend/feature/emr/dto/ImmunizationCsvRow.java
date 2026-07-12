package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImmunizationCsvRow {
    @CsvBindByName(column = "DATE")         private String date;
    @CsvBindByName(column = "PATIENT")      private String patient;
    @CsvBindByName(column = "ENCOUNTER")    private String encounter;
    @CsvBindByName(column = "CODE")         private String code;
    @CsvBindByName(column = "DESCRIPTION")  private String description;
    @CsvBindByName(column = "BASE_COST")    private String baseCost;
}