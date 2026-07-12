package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CareplanCsvRow {
    @CsvBindByName(column = "Id")               private String id;
    @CsvBindByName(column = "START")            private String start;
    @CsvBindByName(column = "STOP")             private String stop;
    @CsvBindByName(column = "PATIENT")          private String patient;
    @CsvBindByName(column = "ENCOUNTER")        private String encounter;
    @CsvBindByName(column = "CODE")             private String code;
    @CsvBindByName(column = "DESCRIPTION")      private String description;
    @CsvBindByName(column = "REASONCODE")       private String reasonCode;
    @CsvBindByName(column = "REASONDESCRIPTION") private String reasonDescription;
}
