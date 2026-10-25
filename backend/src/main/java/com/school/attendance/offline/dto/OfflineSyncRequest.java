package com.school.attendance.offline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OfflineSyncRequest(
    @NotBlank(message = "Source device ID is required")
    String sourceDeviceId,

    @NotBlank(message = "Batch identifier is required")
    String batchIdentifier,

    @NotEmpty(message = "Batch items cannot be empty")
    List<OfflineItem> items
) {
    public record OfflineItem(
        UUID sessionId,
        UUID studentId,
        String status, // PRESENT, LATE, ABSENT
        Instant deviceRecordedAt,
        String idempotencyKey
    ) {}
}
