package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProviderCsvRow {
    @CsvBindByName(column = "Id")               private String id;
    @CsvBindByName(column = "ORGANIZATION")     private String organization;  // synthea org UUID
    @CsvBindByName(column = "NAME")             private String name;
    @CsvBindByName(column = "GENDER")           private String gender;
    @CsvBindByName(column = "SPECIALITY")       private String speciality;
    @CsvBindByName(column = "ADDRESS")          private String address;
    @CsvBindByName(column = "CITY")             private String city;
    @CsvBindByName(column = "STATE")            private String state;
    @CsvBindByName(column = "ZIP")              private String zip;
    @CsvBindByName(column = "LAT")              private String lat;
    @CsvBindByName(column = "LON")              private String lon;
    @CsvBindByName(column = "UTILIZATION")      private String utilization;
}