package com.emrbridge.backend.feature.fhir.converter;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for converting Java date/time types to FHIR-compatible formats.
 * HAPI FHIR requires proper ISO 8601 format with seconds for DateTime types.
 */
public class FhirDateTimeUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Converts LocalDateTime to FHIR DateTimeType with proper format (yyyy-MM-dd'T'HH:mm:ss)
     */
    public static DateTimeType toDateTimeType(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return new DateTimeType(localDateTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * Converts LocalDate to FHIR DateType with proper format (yyyy-MM-dd)
     */
    public static DateType toDateType(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return new DateType(localDate.format(DATE_FORMATTER));
    }

    /**
     * Converts LocalDate to FHIR DateTimeType (for fields that require DateTime but only have Date)
     */
    public static DateTimeType toDateTimeTypeFromDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return new DateTimeType(localDate.format(DATE_FORMATTER));
    }
}
