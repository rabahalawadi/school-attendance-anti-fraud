package com.school.attendance.offline.service;

import com.school.attendance.academic.entity.Student;
import com.school.attendance.academic.repository.StudentRepository;
import com.school.attendance.attendance.entity.*;
import com.school.attendance.attendance.repository.AttendanceRecordRepository;
import com.school.attendance.attendance.repository.AttendanceSessionRepository;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.offline.dto.OfflineSyncRequest;
import com.school.attendance.offline.dto.OfflineSyncResponse;
import com.school.attendance.offline.entity.OfflineSyncBatch;
import com.school.attendance.offline.repository.OfflineSyncBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OfflineSyncService {

    private final OfflineSyncBatchRepository batchRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    public OfflineSyncService(
            OfflineSyncBatchRepository batchRepository,
            AttendanceRecordRepository recordRepository,
            AttendanceSessionRepository sessionRepository,
            StudentRepository studentRepository) {
        this.batchRepository = batchRepository;
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public OfflineSyncResponse processBatch(OfflineSyncRequest request) {
        // Prevent duplicate processing of the same batch identifier
        Optional<OfflineSyncBatch> existingBatch = batchRepository.findByBatchIdentifier(request.batchIdentifier());
        if (existingBatch.isPresent()) {
            OfflineSyncBatch b = existingBatch.get();
            return new OfflineSyncResponse(
                    b.getId(),
                    b.getBatchIdentifier(),
                    b.getSynchronizationStatus(),
                    request.items().size(),
                    0,
                    request.items().size(),
                    0,
                    b.getReceivedAt(),
                    List.of(new OfflineSyncResponse.SyncItemResult(
                            "BATCH", null, "SKIPPED_DUPLICATE", "Batch already processed previously"))
            );
        }

        List<OfflineSyncResponse.SyncItemResult> itemResults = new ArrayList<>();
        int synchronizedCount = 0;
        int duplicateCount = 0;
        int conflictCount = 0;

        Instant now = Instant.now();

        for (OfflineSyncRequest.OfflineItem item : request.items()) {
            // Check idempotency key first
            if (recordRepository.findByIdempotencyKey(item.idempotencyKey()).isPresent()) {
                duplicateCount++;
                itemResults.add(new OfflineSyncResponse.SyncItemResult(
                        item.idempotencyKey(), item.studentId(), "SKIPPED_DUPLICATE", "Record already recorded on server"));
                continue;
            }

            Optional<AttendanceSession> sessionOpt = sessionRepository.findById(item.sessionId());
            Optional<Student> studentOpt = studentRepository.findById(item.studentId());

            if (sessionOpt.isEmpty() || studentOpt.isEmpty()) {
                conflictCount++;
                itemResults.add(new OfflineSyncResponse.SyncItemResult(
                        item.idempotencyKey(), item.studentId(), "ERROR", "Invalid session or student ID"));
                continue;
            }

            AttendanceSession session = sessionOpt.get();
            Student student = studentOpt.get();

            // Check if server already has a record for (session, student)
            Optional<AttendanceRecord> existingRecord = recordRepository
                    .findByAttendanceSessionIdAndStudentId(session.getId(), student.getId());

            if (existingRecord.isPresent()) {
                // Conflict handling: never silently overwrite newer verified server record
                conflictCount++;
                itemResults.add(new OfflineSyncResponse.SyncItemResult(
                        item.idempotencyKey(),
                        student.getId(),
                        "CONFLICT_PRESERVED",
                        "Server already holds record (" + existingRecord.get().getStatus() + "). Retaining server record."));
                continue;
            }

            // Flag suspicious excessive clock drift (e.g. recorded in future or >30 days ago)
            Instant recordedAt = item.deviceRecordedAt() != null ? item.deviceRecordedAt() : now;
            boolean suspiciousClock = recordedAt.isAfter(now.plusSeconds(300)) || recordedAt.isBefore(now.minusSeconds(30L * 24 * 3600));

            AttendanceStatus status = parseStatus(item.status());
            VerificationStatus vStatus = suspiciousClock ? VerificationStatus.FLAGGED : VerificationStatus.OFFLINE_RECORDED;

            AttendanceRecord record = new AttendanceRecord(
                    session,
                    student,
                    status,
                    vStatus,
                    recordedAt,
                    item.idempotencyKey()
            );

            if (suspiciousClock) {
                record.setCorrectionReason("Suspicious clock drift: device recorded at " + recordedAt);
            }

            recordRepository.save(record);
            synchronizedCount++;
            itemResults.add(new OfflineSyncResponse.SyncItemResult(
                    item.idempotencyKey(), student.getId(), "SYNCHRONIZED", "Synchronized successfully with status " + vStatus));
        }

        String batchStatus = (conflictCount == 0 && duplicateCount == 0) ? "PROCESSED" : "PARTIALLY_PROCESSED";
        String summary = String.format("Total: %d, Synchronized: %d, Duplicates: %d, Conflicts: %d",
                request.items().size(), synchronizedCount, duplicateCount, conflictCount);

        OfflineSyncBatch batch = new OfflineSyncBatch(
                request.sourceDeviceId(),
                request.batchIdentifier(),
                now,
                batchStatus,
                summary
        );

        OfflineSyncBatch savedBatch = batchRepository.save(batch);

        return new OfflineSyncResponse(
                savedBatch.getId(),
                savedBatch.getBatchIdentifier(),
                batchStatus,
                request.items().size(),
                synchronizedCount,
                duplicateCount,
                conflictCount,
                now,
                itemResults
        );
    }

    private AttendanceStatus parseStatus(String statusStr) {
        try {
            return AttendanceStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            return AttendanceStatus.PRESENT;
        }
    }
}
