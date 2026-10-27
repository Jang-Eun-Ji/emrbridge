package com.emrbridge.backend.feature.analytics.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospital_dim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Integer hospitalId;

    /** Stable org key from FHIR Organization.identifier (Synthea organization UUID). */
    @Column(name = "original_organization_id", nullable = false, unique = true, length = 100)
    private String originalOrganizationId;

    @Column(name = "hospital_name", nullable = false, length = 300)
    private String hospitalName;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
