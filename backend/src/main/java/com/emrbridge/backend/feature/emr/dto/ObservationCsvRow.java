package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ObservationCsvRow {
    @CsvBindByName(column = "DATE")         private String date;
    @CsvBindByName(column = "PATIENT")      private String patient;
    @CsvBindByName(column = "ENCOUNTER")    private String encounter;
    @CsvBindByName(column = "CATEGORY")     private String category;
    @CsvBindByName(column = "CODE")         private String code;
    @CsvBindByName(column = "DESCRIPTION")  private String description;
    @CsvBindByName(column = "VALUE")        private String value;
    @CsvBindByName(column = "UNITS")        private String units;
    @CsvBindByName(column = "TYPE")         private String type;
}