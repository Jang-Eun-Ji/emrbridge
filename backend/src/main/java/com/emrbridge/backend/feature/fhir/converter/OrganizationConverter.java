package com.emrbridge.backend.feature.fhir.converter;

import com.emrbridge.backend.feature.emr.domain.EmrOrganization;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class OrganizationConverter {

    /**
     * Converts EmrOrganization to FHIR Organization resource
     * @param emr source EMR organization entity
     * @return FHIR Organization resource
     */
    public Organization toFhir(EmrOrganization emr) {
        Organization org = new Organization();

        // Synthea ID를 identifier로 설정
        org.addIdentifier()
                .setSystem("http://emrbridge.com/organization-id")
                .setValue(emr.getSyntheaId());

        org.setName(emr.getName());

        // 전화번호
        if (emr.getPhone() != null) {
            org.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(emr.getPhone());
        }

        // 주소
        if (emr.getAddress() != null) {
            org.addAddress()
                    .addLine(emr.getAddress())
                    .setCity(emr.getCity())
                    .setState(emr.getState())
                    .setPostalCode(emr.getZip());
        }

        return org;
    }
}