package com.school.attendance.attendance.dto;

import com.school.attendance.attendance.entity.AttendanceStatus;
import com.school.attendance.attendance.entity.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record AttendanceRecordResponse(
    UUID id,
    UUID sessionId,
    String subjectName,
    String className,
    String teacherName,
    UUID studentId,
    String studentNumber,
    String studentName,
    AttendanceStatus status,
    VerificationStatus verificationStatus,
    Instant recordedAt,
    Instant serverReceivedAt,
    String confirmedByName,
    Instant confirmedAt,
    String correctionReason
) {}
