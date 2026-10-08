package com.emrbridge.backend.feature.analytics.service;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FhirExtractService {

    private static final int PAGE_SIZE = 500;

    private final IGenericClient fhirClient;

    public List<Patient> fetchAllPatients() {
        return fetchAll(Patient.class);
    }

    public List<Encounter> fetchAllEncounters() {
        return fetchAll(Encounter.class);
    }

    public List<Condition> fetchAllConditions() {
        return fetchAll(Condition.class);
    }

    public List<Organization> fetchAllOrganizations() {
        return fetchAll(Organization.class);
    }

    public long countPatients() {
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .summaryMode(SummaryEnum.COUNT)
                .returnBundle(Bundle.class)
                .execute();
        return Math.max(bundle.getTotal(), 0);
    }

    private <T extends Resource> List<T> fetchAll(Class<T> resourceType) {
        List<T> results = new ArrayList<>();
        Bundle bundle = fhirClient.search()
                .forResource(resourceType)
                .returnBundle(Bundle.class)
                .count(PAGE_SIZE)
                .execute();

        while (bundle != null) {
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                IBaseResource resource = entry.getResource();
                if (resourceType.isInstance(resource)) {
                    results.add(resourceType.cast(resource));
                }
            }

            Bundle.BundleLinkComponent nextLink = bundle.getLink(Bundle.LINK_NEXT);
            if (nextLink != null && nextLink.getUrl() != null) {
                bundle = fhirClient.loadPage()
                        .byUrl(nextLink.getUrl())
                        .andReturnBundle(Bundle.class)
                        .execute();
            } else {
                break;
            }
        }

        log.info("FHIR {} 추출 완료: {}건", resourceType.getSimpleName(), results.size());
        return results;
    }
}
