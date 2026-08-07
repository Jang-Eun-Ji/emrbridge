package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrMedication;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import static com.emrbridge.backend.feature.fhir.converter.FhirDateTimeUtil.toDateTimeType;

@Component
public class MedicationConverter {

    public MedicationRequest toFhir(EmrMedication emr) {
        MedicationRequest request = new MedicationRequest();

        request.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
        request.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        request.setSubject(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            request.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCode() != null) {
            request.setMedication(new CodeableConcept().addCoding(
                    new Coding()
                            .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
                            .setCode(emr.getCode())
                            .setDisplay(emr.getDescription())
            ));
        }

        if (emr.getStartTime() != null) {
            request.setAuthoredOnElement(toDateTimeType(emr.getStartTime()));
        }

        if (emr.getReasonCode() != null) {
            request.addReasonCode().addCoding()
                    .setCode(emr.getReasonCode())
                    .setDisplay(emr.getReasonDescription());
        }

        return request;
    }
}