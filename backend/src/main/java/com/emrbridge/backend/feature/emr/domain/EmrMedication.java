package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


import java.time.LocalDateTime;

@Entity
@Table(name = "emr_medication")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrMedication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private EmrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private EmrEncounter encounter;

    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private String code;
    private String description;

    @Column(precision = 10, scale = 2) private BigDecimal baseCost;
    @Column(precision = 10, scale = 2) private BigDecimal payerCoverage;
    private Integer dispenses;
    @Column(precision = 10, scale = 2) private BigDecimal totalCost;
    private String reasonCode;
    private String reasonDescription;
}