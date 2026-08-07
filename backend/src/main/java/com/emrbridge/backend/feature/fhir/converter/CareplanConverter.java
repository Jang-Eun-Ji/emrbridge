package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrCareplan;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import static com.emrbridge.backend.feature.fhir.converter.FhirDateTimeUtil.toDateTimeTypeFromDate;

@Component
public class CareplanConverter {

    public CarePlan toFhir(EmrCareplan emr) {
        CarePlan carePlan = new CarePlan();

        carePlan.addIdentifier()
                .setSystem("http://emrbridge.com/careplan-id")
                .setValue(emr.getSyntheaId());

        carePlan.setStatus(CarePlan.CarePlanStatus.COMPLETED);
        carePlan.setIntent(CarePlan.CarePlanIntent.PLAN);

        carePlan.setSubject(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            carePlan.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCode() != null) {
            carePlan.addCategory().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode(emr.getCode())
                    .setDisplay(emr.getDescription());
        }

        if (emr.getStartDate() != null || emr.getStopDate() != null) {
            Period period = new Period();
            if (emr.getStartDate() != null) period.setStartElement(toDateTimeTypeFromDate(emr.getStartDate()));
            if (emr.getStopDate() != null) period.setEndElement(toDateTimeTypeFromDate(emr.getStopDate()));
            carePlan.setPeriod(period);
        }

        return carePlan;
    }
}