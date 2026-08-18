package com.emrbridge.backend.feature.fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient; // HAPI FHIR 클라이언트 인터페이스
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// HAPI FHIR 클라이언트 설정 클래스
@Configuration
public class FhirConfig {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) { // HAPI FHIR 클라이언트 빈 생성
        return fhirContext.newRestfulGenericClient(fhirServerUrl);
    }
}