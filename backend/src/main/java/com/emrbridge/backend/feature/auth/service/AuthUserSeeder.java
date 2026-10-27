package com.emrbridge.backend.feature.auth.service;

import com.emrbridge.backend.feature.auth.domain.AppUser;
import com.emrbridge.backend.feature.auth.domain.AppUserRole;
import com.emrbridge.backend.feature.auth.repository.AppUserRepository;
import com.emrbridge.backend.feature.emr.domain.EmrOrganization;
import com.emrbridge.backend.feature.emr.repository.EmrOrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Seeds demo accounts on startup:
 * <ul>
 *   <li>admin / admin123 — sees all hospitals</li>
 *   <li>one HOSPITAL user per EMR organization (password hospital123)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUserSeeder implements ApplicationRunner {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String HOSPITAL_PASSWORD = "hospital123";

    private final AppUserRepository appUserRepository;
    private final EmrOrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(transactionManager = "analyticsTransactionManager")
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedHospitalUsers();
    }

    private void seedAdmin() {
        if (appUserRepository.existsByUsername(ADMIN_USERNAME)) {
            return;
        }
        appUserRepository.save(AppUser.builder()
                .username(ADMIN_USERNAME)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(AppUserRole.ADMIN)
                .enabled(true)
                .build());
        log.info("Seeded admin user '{}' (password: {})", ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private void seedHospitalUsers() {
        List<EmrOrganization> organizations = organizationRepository.findAll();
        if (organizations.isEmpty()) {
            log.info("No EMR organizations yet — hospital users will be seeded after CSV import");
            return;
        }

        Set<String> usedUsernames = new HashSet<>();
        appUserRepository.findAll().forEach(u -> usedUsernames.add(u.getUsername()));

        int created = 0;
        for (EmrOrganization org : organizations) {
            if (org.getSyntheaId() == null || org.getSyntheaId().isBlank()) {
                continue;
            }
            String username = uniqueUsername(org, usedUsernames);
            if (appUserRepository.existsByUsername(username)) {
                continue;
            }
            if (appUserRepository.existsByOrganizationSyntheaId(org.getSyntheaId())) {
                continue;
            }

            appUserRepository.save(AppUser.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(HOSPITAL_PASSWORD))
                    .role(AppUserRole.HOSPITAL)
                    .organizationSyntheaId(org.getSyntheaId())
                    .hospitalName(org.getName() != null ? org.getName() : username)
                    .enabled(true)
                    .build());
            usedUsernames.add(username);
            created++;
        }

        if (created > 0) {
            log.info("Seeded {} hospital users (password: {})", created, HOSPITAL_PASSWORD);
        }
    }

    private String uniqueUsername(EmrOrganization org, Set<String> used) {
        String base = slugify(org.getName());
        if (base.isBlank() || "admin".equals(base)) {
            base = "hospital_" + org.getSyntheaId().substring(0, 8);
        }
        String candidate = base;
        int i = 2;
        while (used.contains(candidate) || ADMIN_USERNAME.equals(candidate)) {
            candidate = base + "_" + i++;
        }
        return candidate;
    }

    private static String slugify(String name) {
        if (name == null) {
            return "";
        }
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (slug.length() > 40) {
            slug = slug.substring(0, 40).replaceAll("_+$", "");
        }
        return slug;
    }
}
