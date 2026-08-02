package com.emrbridge.backend.feature.emr.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientCsvRow {

    @CsvBindByName(column = "Id")
    private String id;

    @CsvBindByName(column = "SSN")
    private String ssn;

    @CsvBindByName(column = "BIRTHDATE")
    private String birthDate;

    @CsvBindByName(column = "DEATHDATE")
    private String deathDate;

    @CsvBindByName(column = "FIRST")
    private String first;

    @CsvBindByName(column = "LAST")
    private String last;

    @CsvBindByName(column = "GENDER")
    private String gender;

    @CsvBindByName(column = "RACE")
    private String race;

    @CsvBindByName(column = "ETHNICITY")
    private String ethnicity;

    @CsvBindByName(column = "ADDRESS")
    private String address;

    @CsvBindByName(column = "CITY")
    private String city;

    @CsvBindByName(column = "STATE")
    private String state;

    @CsvBindByName(column = "COUNTY")
    private String county;

    @CsvBindByName(column = "ZIP")
    private String zip;

    @CsvBindByName(column = "LAT")
    private String lat;

    @CsvBindByName(column = "LON")
    private String lon;

    @CsvBindByName(column = "HEALTHCARE_EXPENSES")
    private String healthcareExpenses;

    @CsvBindByName(column = "HEALTHCARE_COVERAGE")
    private String healthcareCoverage;
}