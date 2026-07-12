// ProviderConverter.java
package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrProvider;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class ProviderConverter {

    /**
     * Converts EmrProvider to FHIR Practitioner resource
     * @param emr source EMR provider entity
     * @return FHIR Practitioner resource
     */
    public Practitioner toFhir(EmrProvider emr) {
        Practitioner practitioner = new Practitioner();

        // Synthea ID를 identifier로 설정
        practitioner.addIdentifier()
                .setSystem("http://emrbridge.com/provider-id")
                .setValue(emr.getSyntheaId());

        // 이름
        if (emr.getName() != null) {
            practitioner.addName()
                    .setFamily(emr.getName());
        }

        // 성별
        if (emr.getGender() != null) {
            switch (emr.getGender().toUpperCase()) {
                case "M" -> practitioner.setGender(Enumerations.AdministrativeGender.MALE);
                case "F" -> practitioner.setGender(Enumerations.AdministrativeGender.FEMALE);
                default  -> practitioner.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        // 전문과목
        if (emr.getSpeciality() != null) {
            practitioner.addQualification().getCode().addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setDisplay(emr.getSpeciality());
        }

        // 주소
        if (emr.getAddress() != null) {
            practitioner.addAddress()
                    .addLine(emr.getAddress())
                    .setCity(emr.getCity())
                    .setState(emr.getState())
                    .setPostalCode(emr.getZip());
        }

        return practitioner;
    }
}