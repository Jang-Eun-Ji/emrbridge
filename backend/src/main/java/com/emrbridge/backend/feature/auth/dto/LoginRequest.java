package com.emrbridge.backend.feature.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "사용자 ID", example = "admin")
        @NotBlank String username,
        @Schema(description = "비밀번호", example = "admin123")
        @NotBlank String password
) {
}
