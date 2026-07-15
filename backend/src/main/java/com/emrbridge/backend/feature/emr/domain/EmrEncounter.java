package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(name = "emr_encounter")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrEncounter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "synthea_id", nullable = false, unique = true)
    private String syntheaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private EmrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private EmrOrganization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private EmrProvider provider;

    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private String encounterClass;
    private String code;
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal baseEncounterCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalClaimCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal payerCoverage;

    private String reasonCode;
    private String reasonDescription;
}