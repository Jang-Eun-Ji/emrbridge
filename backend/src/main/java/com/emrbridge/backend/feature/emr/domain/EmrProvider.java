package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emr_provider")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrProvider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "synthea_id", nullable = false, unique = true)
    private String syntheaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private EmrOrganization organization;

    private String name;
    private String gender;
    private String speciality;
    private String address;
    private String city;
    private String state;
    private String zip;
    private Double lat;
    private Double lon;
    private Integer utilization;
}
