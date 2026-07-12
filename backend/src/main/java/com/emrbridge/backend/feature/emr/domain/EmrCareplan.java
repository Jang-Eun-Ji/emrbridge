package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "emr_careplan")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrCareplan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "synthea_id", nullable = false, unique = true)
    private String syntheaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private EmrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private EmrEncounter encounter;

    private LocalDate startDate;
    private LocalDate stopDate;
    private String code;
    private String description;
    private String reasonCode;
    private String reasonDescription;
}