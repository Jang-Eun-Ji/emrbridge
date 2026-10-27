package com.emrbridge.backend.feature.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppUserRole role;

    /** Stable Synthea organization UUID; null for ADMIN. */
    @Column(name = "organization_synthea_id", length = 100)
    private String organizationSyntheaId;

    @Column(name = "hospital_name", length = 300)
    private String hospitalName;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
