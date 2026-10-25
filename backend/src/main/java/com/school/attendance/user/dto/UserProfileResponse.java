package com.school.attendance.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String username,
    String role,
    String status,
    UUID profileId,
    String identifier,
    String fullName,
    String email,
    Instant lastLoginAt
) {}
