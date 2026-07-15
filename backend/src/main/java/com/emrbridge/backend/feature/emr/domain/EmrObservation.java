package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emr_observation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrObservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private EmrPatient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private EmrEncounter encounter;

    private LocalDateTime obsDate;
    private String category;
    private String code;
    private String description;
    private String value;
    private String units;
    private String obsType;
}

