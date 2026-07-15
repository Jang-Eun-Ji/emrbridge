package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "emr_allergy")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrAllergy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private EmrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private EmrEncounter encounter;

    private LocalDate startDate;
    private LocalDate stopDate;
    private String code;
    private String system;
    private String description;
    private String allergyType;
    private String category;
    private String reaction1Code;
    private String reaction1Desc;
    private String reaction2Code;
    private String reaction2Desc;
}