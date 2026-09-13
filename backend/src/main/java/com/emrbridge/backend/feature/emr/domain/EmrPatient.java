package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emr_patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmrPatient {

    // Primary key for the EmrPatient entity.
    // auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Original SSN value from the Synthea source CSV (US format, XXX-XX-XXXX).
     * Used as the unique cross-EMR patient identifier.
     */
    @Column(name = "ssn", unique = true, length = 11)
    private String ssn;

    // Unique patient identifier from the Synthea source CSV (UUID format).
    @Column(name = "synthea_patient_id", nullable = false, unique = true, length = 100)
    private String syntheaPatientId;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "death_date")
    private LocalDate deathDate;

    @Column(length = 100)
    private String race;

    @Column(length = 100)
    private String ethnicity;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String county;

    @Column(length = 20)
    private String zip;

    private Double lat;

    private Double lon;

    @Column(name = "healthcare_expenses")
    private BigDecimal healthcareExpenses;

    @Column(name = "healthcare_coverage")
    private BigDecimal healthcareCoverage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}