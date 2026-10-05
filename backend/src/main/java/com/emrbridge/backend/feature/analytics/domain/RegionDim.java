package com.emrbridge.backend.feature.analytics.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "region_dim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionDim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer regionId;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 100)
    private String county;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "region_name", nullable = false, unique = true, length = 300)
    private String regionName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
