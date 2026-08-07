package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrObservation;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import static com.emrbridge.backend.feature.fhir.converter.FhirDateTimeUtil.toDateTimeType;

@Component
public class ObservationConverter {

    public Observation toFhir(EmrObservation emr) {
        Observation observation = new Observation();

        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.setSubject(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            observation.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCategory() != null) {
            observation.addCategory().addCoding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                    .setCode(emr.getCategory());
        }

        if (emr.getCode() != null) {
            observation.setCode(new CodeableConcept().addCoding(
                    new Coding()
                            .setSystem("http://loinc.org")
                            .setCode(emr.getCode())
                            .setDisplay(emr.getDescription())
            ));
        }

        if (emr.getObsDate() != null) {
            observation.setEffective(toDateTimeType(emr.getObsDate()));
        }

        if (emr.getValue() != null) {
            try {
                double val = Double.parseDouble(emr.getValue());
                Quantity quantity = new Quantity();
                quantity.setValue(val);
                if (emr.getUnits() != null) quantity.setUnit(emr.getUnits());
                observation.setValue(quantity);
            } catch (NumberFormatException e) {
                observation.setValue(new StringType(emr.getValue()));
            }
        }

        return observation;
    }
}