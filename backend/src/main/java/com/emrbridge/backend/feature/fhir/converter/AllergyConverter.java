package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrAllergy;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class AllergyConverter {

    public AllergyIntolerance toFhir(EmrAllergy emr) {
        AllergyIntolerance allergy = new AllergyIntolerance();

        allergy.setPatient(new Reference("Patient?identifier=" + emr.getPatient().getSyntheaPatientId()));

        if (emr.getEncounter() != null) {
            allergy.setEncounter(new Reference("Encounter?identifier=" + emr.getEncounter().getSyntheaId()));
        }

        if (emr.getCode() != null) {
            allergy.setCode(new CodeableConcept().addCoding(
                    new Coding()
                            .setSystem(emr.getSystem() != null ? emr.getSystem() : "http://snomed.info/sct")
                            .setCode(emr.getCode())
                            .setDisplay(emr.getDescription())
            ));
        }

        if (emr.getAllergyType() != null) {
            try {
                allergy.setType(AllergyIntolerance.AllergyIntoleranceType.fromCode(emr.getAllergyType().toLowerCase()));
            } catch (Exception ignored) {}
        }

        if (emr.getCategory() != null) {
            try {
                allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.fromCode(emr.getCategory().toLowerCase()));
            } catch (Exception ignored) {}
        }

        allergy.setClinicalStatus(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical")
                        .setCode(emr.getStopDate() != null ? "inactive" : "active")
        ));

        // 반응 추가
        if (emr.getReaction1Code() != null) {
            allergy.addReaction().addManifestation().addCoding()
                    .setCode(emr.getReaction1Code())
                    .setDisplay(emr.getReaction1Desc());
        }
        if (emr.getReaction2Code() != null) {
            allergy.addReaction().addManifestation().addCoding()
                    .setCode(emr.getReaction2Code())
                    .setDisplay(emr.getReaction2Desc());
        }

        return allergy;
    }
}