package com.emrbridge.backend.feature.auth.security;

import com.emrbridge.backend.feature.auth.config.JwtProperties;
import com.emrbridge.backend.feature.auth.domain.AppUserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String createToken(AuthUserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        var builder = Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("role", principal.getRole().name())
                .issuedAt(now)
                .expiration(expiry);

        if (principal.getOrganizationSyntheaId() != null) {
            builder.claim("orgId", principal.getOrganizationSyntheaId());
        }
        if (principal.getHospitalName() != null) {
            builder.claim("hospitalName", principal.getHospitalName());
        }

        return builder.signWith(signingKey()).compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthUserPrincipal toPrincipal(Claims claims) {
        AppUserRole role = AppUserRole.valueOf(claims.get("role", String.class));
        return new AuthUserPrincipal(
                claims.get("uid", Number.class).longValue(),
                claims.getSubject(),
                "",
                role,
                claims.get("orgId", String.class),
                claims.get("hospitalName", String.class),
                true
        );
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
