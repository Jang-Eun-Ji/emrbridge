package com.emrbridge.backend.feature.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
    public static LoginResponse bearer(String token, UserResponse user) {
        return new LoginResponse(token, "Bearer", user);
    }
}
