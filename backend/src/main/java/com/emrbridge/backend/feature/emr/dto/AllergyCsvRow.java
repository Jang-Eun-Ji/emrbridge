package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AllergyCsvRow {
    @CsvBindByName(column = "START")        private String start;
    @CsvBindByName(column = "STOP")         private String stop;
    @CsvBindByName(column = "PATIENT")      private String patient;
    @CsvBindByName(column = "ENCOUNTER")    private String encounter;
    @CsvBindByName(column = "CODE")         private String code;
    @CsvBindByName(column = "SYSTEM")       private String system;
    @CsvBindByName(column = "DESCRIPTION")  private String description;
    @CsvBindByName(column = "TYPE")         private String type;
    @CsvBindByName(column = "CATEGORY")     private String category;
    @CsvBindByName(column = "REACTION1")    private String reaction1Code;
    @CsvBindByName(column = "DESCRIPTION1") private String reaction1Desc;
    @CsvBindByName(column = "REACTION2")    private String reaction2Code;
    @CsvBindByName(column = "DESCRIPTION2") private String reaction2Desc;
}
