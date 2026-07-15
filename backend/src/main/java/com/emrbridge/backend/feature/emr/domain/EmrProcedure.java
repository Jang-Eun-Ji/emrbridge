package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


import java.time.LocalDateTime;

@Entity
@Table(name = "emr_procedure")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrProcedure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private EmrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private EmrEncounter encounter;

    private LocalDateTime procDate;
    private String code;
    private String description;
    @Column(precision = 10, scale = 2) private BigDecimal baseCost;
    private String reasonCode;
    private String reasonDescription;
}