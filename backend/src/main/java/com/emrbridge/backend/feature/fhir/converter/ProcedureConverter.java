package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrProcedure;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import static com.emrbridge.backend.feature.fhir.converter.FhirDateTimeUtil.toDateTimeType;

@Component
public class ProcedureConverter {

    public Procedure toFhir(EmrProcedure emr) {
        Procedure procedure = new Procedure();

        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        procedure.setSubject(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            procedure.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCode() != null) {
            procedure.setCode(new CodeableConcept().addCoding(
                    new Coding()
                            .setSystem("http://snomed.info/sct")
                            .setCode(emr.getCode())
                            .setDisplay(emr.getDescription())
            ));
        }

        if (emr.getProcDate() != null) {
            procedure.setPerformed(toDateTimeType(emr.getProcDate()));
        }

        if (emr.getReasonCode() != null) {
            procedure.addReasonCode().addCoding()
                    .setCode(emr.getReasonCode())
                    .setDisplay(emr.getReasonDescription());
        }

        return procedure;
    }
}