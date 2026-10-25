package com.school.attendance.offline.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.offline.dto.OfflineSyncRequest;
import com.school.attendance.offline.dto.OfflineSyncResponse;
import com.school.attendance.offline.entity.OfflineSyncBatch;
import com.school.attendance.offline.repository.OfflineSyncBatchRepository;
import com.school.attendance.offline.service.OfflineSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offline-sync")
@Tag(name = "Offline Synchronization", description = "Batch synchronization of offline-recorded attendance records")
public class OfflineSyncController {

    private final OfflineSyncService syncService;
    private final OfflineSyncBatchRepository batchRepository;

    public OfflineSyncController(OfflineSyncService syncService, OfflineSyncBatchRepository batchRepository) {
        this.syncService = syncService;
        this.batchRepository = batchRepository;
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Submit an offline attendance batch for synchronization")
    public ResponseEntity<ApiResponse<OfflineSyncResponse>> submitBatch(
            @Valid @RequestBody OfflineSyncRequest request) {
        OfflineSyncResponse response = syncService.processBatch(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Offline batch processed"));
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get batch processing details")
    public ResponseEntity<ApiResponse<OfflineSyncBatch>> getBatch(@PathVariable UUID id) {
        OfflineSyncBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Sync batch not found"));
        return ResponseEntity.ok(ApiResponse.ok(batch));
    }
}
