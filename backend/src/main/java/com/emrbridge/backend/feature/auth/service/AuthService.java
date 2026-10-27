package com.emrbridge.backend.feature.auth.service;

import com.emrbridge.backend.feature.auth.domain.AppUser;
import com.emrbridge.backend.feature.auth.dto.LoginRequest;
import com.emrbridge.backend.feature.auth.dto.LoginResponse;
import com.emrbridge.backend.feature.auth.dto.UserResponse;
import com.emrbridge.backend.feature.auth.repository.AppUserRepository;
import com.emrbridge.backend.feature.auth.security.AuthUserPrincipal;
import com.emrbridge.backend.feature.auth.security.JwtService;
import com.emrbridge.backend.feature.auth.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(transactionManager = "analyticsTransactionManager", readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        AuthUserPrincipal principal = new AuthUserPrincipal(user);
        String token = jwtService.createToken(principal);
        return LoginResponse.bearer(token, UserResponse.from(principal));
    }

    public UserResponse me() {
        return UserResponse.from(SecurityUtils.requireCurrentUser());
    }
}
