package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrCondition;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class ConditionConverter {

    public Condition toFhir(EmrCondition emr) {
        Condition condition = new Condition();

        condition.setSubject(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            condition.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCode() != null) {
            condition.setCode(new CodeableConcept().addCoding(
                    new Coding()
                            .setSystem("http://snomed.info/sct")
                            .setCode(emr.getCode())
                            .setDisplay(emr.getDescription())
            ));
        }

        if (emr.getStartDate() != null) {
            condition.setOnset(new DateTimeType(emr.getStartDate().toString()));
        }

        if (emr.getStopDate() != null) {
            condition.setAbatement(new DateTimeType(emr.getStopDate().toString()));
        }

        condition.setClinicalStatus(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                        .setCode(emr.getStopDate() != null ? "resolved" : "active")
        ));

        return condition;
    }
}