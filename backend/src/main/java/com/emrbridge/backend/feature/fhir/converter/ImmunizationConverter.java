package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrImmunization;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import static com.emrbridge.backend.feature.fhir.converter.FhirDateTimeUtil.toDateTimeType;

@Component
public class ImmunizationConverter {

    public Immunization toFhir(EmrImmunization emr) {
        Immunization immunization = new Immunization();

        immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

        immunization.setPatient(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            immunization.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCode() != null) {
            immunization.setVaccineCode(new CodeableConcept().addCoding(
                    new Coding()
                            .setSystem("http://hl7.org/fhir/sid/cvx")
                            .setCode(emr.getCode())
                            .setDisplay(emr.getDescription())
            ));
        }

        if (emr.getImmDate() != null) {
            immunization.setOccurrence(toDateTimeType(emr.getImmDate()));
        }

        immunization.setPrimarySource(true);

        return immunization;
    }
}