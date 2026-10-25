package com.school.attendance.offline.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OfflineSyncResponse(
    UUID batchId,
    String batchIdentifier,
    String synchronizationStatus,
    int totalItems,
    int synchronizedItems,
    int skippedDuplicateItems,
    int conflictItems,
    Instant processedAt,
    List<SyncItemResult> itemResults
) {
    public record SyncItemResult(
        String idempotencyKey,
        UUID studentId,
        String outcome, // SYNCHRONIZED, SKIPPED_DUPLICATE, CONFLICT_PRESERVED, ERROR
        String detail
    ) {}
}
