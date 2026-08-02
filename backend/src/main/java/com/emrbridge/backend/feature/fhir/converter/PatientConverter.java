package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class PatientConverter {

    // FHIR's officially recognized identifier system URI for US Social
    // Security Numbers. Using the standard URI (instead of a custom one)
    // means any FHIR-compliant consumer can recognize this identifier as
    // an SSN without needing to know about emrbridge-specific conventions.
    private static final String US_SSN_SYSTEM = "http://hl7.org/fhir/sid/us-ssn";

    public Patient toFhir(EmrPatient emr) {
        Patient patient = new Patient();

        // ID (Synthea ID를 FHIR identifier로)
        patient.addIdentifier()
                .setSystem("http://emrbridge.com/patient-id")
                .setValue(emr.getSyntheaPatientId());

        // SSN을 FHIR 표준 US-SSN identifier로 추가.
        // This is the cross-EMR identifier: the same person seen at a
        // different organization should carry the same SSN value here,
        // allowing FHIR consumers (and conditional-create matching) to
        // recognize them as the same individual even if their
        // syntheaPatientId differs across sources.
        if (emr.getSsn() != null && !emr.getSsn().isBlank()) {
            patient.addIdentifier()
                    .setSystem(US_SSN_SYSTEM)
                    .setValue(emr.getSsn());
        }

        // 이름
        patient.addName()
                .setFamily(emr.getLastName())
                .addGiven(emr.getFirstName());

        // 성별
        if (emr.getGender() != null) {
            switch (emr.getGender().toUpperCase()) {
                case "M" -> patient.setGender(Enumerations.AdministrativeGender.MALE);
                case "F" -> patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                default  -> patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        // 생년월일
        if (emr.getBirthDate() != null) {
            patient.setBirthDateElement(new DateType(emr.getBirthDate().toString()));
        }

        // 사망일 (있으면)
        if (emr.getDeathDate() != null) {
            DateTimeType deceased = new DateTimeType(emr.getDeathDate().toString());
            patient.setDeceased(deceased);
        }

        // 주소
        if (emr.getAddress() != null) {
            patient.addAddress()
                    .addLine(emr.getAddress())
                    .setCity(emr.getCity())
                    .setState(emr.getState())
                    .setPostalCode(emr.getZip());
        }

        // 인종/민족 (extension)
        if (emr.getRace() != null) {
            patient.addExtension()
                    .setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race")
                    .setValue(new StringType(emr.getRace()));
        }

        return patient;
    }
}