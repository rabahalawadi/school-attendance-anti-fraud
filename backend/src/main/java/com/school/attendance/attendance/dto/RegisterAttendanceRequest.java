package com.school.attendance.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterAttendanceRequest(
    @NotNull(message = "Session ID is required")
    UUID sessionId,

    String qrToken,
    String sixDigitCode,
    String deviceCredential,
    Double latitude,
    Double longitude,
    Double accuracyMeters,
    String idempotencyKey
) {}
