package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "emr_careplan")
@Getter
@Setter
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
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    private String reasonCode;
    @Column(name = "reason_description", columnDefinition = "TEXT")
    private String reasonDescription;
}