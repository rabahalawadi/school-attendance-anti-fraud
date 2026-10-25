package com.school.attendance.user.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    UUID userId,
    String username,
    String role,
    UUID profileId,
    String fullName
) {
    public AuthResponse(String accessToken, String refreshToken, UUID userId, String username, String role, UUID profileId, String fullName) {
        this(accessToken, refreshToken, "Bearer", userId, username, role, profileId, fullName);
    }
}
