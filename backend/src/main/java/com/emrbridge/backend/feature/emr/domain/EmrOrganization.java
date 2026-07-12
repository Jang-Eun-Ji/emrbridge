package com.emrbridge.backend.feature.emr.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emr_organization")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrOrganization {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "synthea_id", nullable = false, unique = true)
    private String syntheaId;

    private String name;
    private String address;
    private String city;
    private String state;
    private String zip;
    private Double lat;
    private Double lon;
    private String phone;
}