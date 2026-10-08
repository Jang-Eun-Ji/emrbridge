package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrEncounter;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import static com.emrbridge.backend.feature.fhir.converter.FhirDateTimeUtil.toDateTimeType;

@Component
public class EncounterConverter {

    public Encounter toFhir(EmrEncounter emr) {
        Encounter encounter = new Encounter();

        encounter.addIdentifier()
                .setSystem("http://emrbridge.com/encounter-id")
                .setValue(emr.getSyntheaId());

        encounter.setStatus(Encounter.EncounterStatus.FINISHED);

        if (emr.getEncounterClass() != null) {
            encounter.setClass_(new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                    .setCode(emr.getEncounterClass()));
        }

        encounter.setSubject(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        // Link visit to the treating hospital (FHIR Organization) so Analytics ETL
        // can populate hospital_dim / visit_facts.hospital_id.
        if (emr.getOrganization() != null && emr.getOrganization().getSyntheaId() != null) {
            Reference hospitalRef = new Reference(
                    "Organization?identifier=http://emrbridge.com/organization-id|"
                            + emr.getOrganization().getSyntheaId());
            if (emr.getOrganization().getName() != null) {
                hospitalRef.setDisplay(emr.getOrganization().getName());
            }
            encounter.setServiceProvider(hospitalRef);
        }

        if (emr.getStartTime() != null) {
            Period period = new Period();
            period.setStartElement(toDateTimeType(emr.getStartTime()));
            if (emr.getStopTime() != null) {
                period.setEndElement(toDateTimeType(emr.getStopTime()));
            }
            encounter.setPeriod(period);
        }

        if (emr.getCode() != null) {
            encounter.addType().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode(emr.getCode())
                    .setDisplay(emr.getDescription());
        }

        if (emr.getReasonCode() != null) {
            encounter.addReasonCode().addCoding()
                    .setCode(emr.getReasonCode())
                    .setDisplay(emr.getReasonDescription());
        }

        return encounter;
    }
}