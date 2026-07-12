package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrPatient;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class PatientConverter {

    public Patient toFhir(EmrPatient emr) {
        Patient patient = new Patient();

        // ID (Synthea ID를 FHIR identifier로)
        patient.addIdentifier()
                .setSystem("http://emrbridge.com/patient-id")
                .setValue(emr.getSyntheaPatientId());

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